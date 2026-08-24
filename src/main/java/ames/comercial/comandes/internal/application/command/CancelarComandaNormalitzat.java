package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzatImpl;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.service.CalculStockAlliberat;
import ames.comercial.comandes.internal.domain.service.IRecalculReservesArticleclient;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.service.CalculCanvisLiniesComanda;
import ames.comercial.comandes.service.CalculadoraComandaNormalitzat;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.TarifesCalculadoraReq;
import ames.comercial.comandes.service.response.MissatgesCanvisComandaResponse;
import ames.comercial.shared.Empresa;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CancelarComandaNormalitzat {

	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	CalculadoraComandaNormalitzat calculadoraComandaNormalitzat;
	IRecalculReservesArticleclient recalculReservesArticleclient;
	ActualitzarEstatComanda actualitzarEstatComanda;

	public CancelarComandaNormalitzat(ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
									  LiniaComandaRepository liniaRepo, CalculadoraComandaNormalitzat calculadoraComandaNormalitzat,
									  IRecalculReservesArticleclient recalculReservesArticleclient,
									  ActualitzarEstatComanda actualitzarEstatComanda) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.calculadoraComandaNormalitzat = calculadoraComandaNormalitzat;
		this.recalculReservesArticleclient = recalculReservesArticleclient;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@Transactional
	public MissatgesCanvisComandaResponse executar (long idComanda) {
		// S'obté la comanda
		var comanda = comandaRepo.find(idComanda).orElseThrow(ComandaNoExisteix::new);
		// Obtenció de les línies de la comanda
		var liniesOriginals = liniaRepo.findByComanda(idComanda);
		// Empresa de la comanda
		var empresa = Empresa.getByClau(comanda.dades().empresa());
		// Obtenció de les dades de càlcul de normalitzat
		var dadesNorm = comanda.dadesNormalitzat().orElseThrow();
		var tarifesCalc = dadesNorm.tarifes();
		// Divisió de les línies originals entre servides i no servides
		var liniesOriginalsPartides = liniesOriginals.stream().collect(Collectors.partitioningBy(LiniaComanda::servida));
		var liniesOriginalsServides = liniesOriginalsPartides.get(true);
		var liniesOriginalsPendents = liniesOriginalsPartides.get(false);

		// Es cancel·len totes les línies pendents i es modifiquen a BBDD
		var liniesCancelades = liniesOriginalsPendents.stream()
				.map(LiniaComanda::cancelar)
				.toList();
		liniaRepo.save(liniesCancelades);

		// Les noves línies de la comanda seran les ja servides + les noves cancel·lades
		var newLinies = Stream.concat(liniesOriginalsServides.stream(), liniesCancelades.stream()).toList();

		// Calculadora de preus amb les línies cancel·lades (ja tenen la quantitat canviada)
		var reqCalc = LiniaNormalitzatReq.of(newLinies);
		var respCalc = calculadoraComandaNormalitzat.calcula(comanda.dades().client(), TarifesCalculadoraReq.from(tarifesCalc), reqCalc);
		var dif = new CalculCanvisLiniesComanda(liniesOriginals, respCalc).calcula();

		// Com ja no quedarà res pendents es marca la comanda com a servida i s'actualitzen els dades de càlcul normalitzades (excepte la divisa que no canvia)
		comanda.marcarServida();
		comanda.canviarDadesNormalitzat(DadesNormalitzatImpl.builder()
				.from(dadesNorm)
				.importNet(respCalc.importNet())
				.importBrut(respCalc.importBrut())
				.pes(respCalc.pes())
				.costTransport(respCalc.costTransport().orElse(BigDecimal.ZERO))
				.build());
		comandaRepo.save(comanda);

		// Càlcul de l'stock alliberat de cada articleclient
		CalculStockAlliberat calculStockAlliberat = new CalculStockAlliberat(liniesOriginals, newLinies);
		var stockAlliberat = calculStockAlliberat.calcula();
		// Per cada articleclient que ha alliberat reserves es recalculen les reserves
		stockAlliberat.forEach((articleClient, reservaAlliberada) -> {
			recalculReservesArticleclient.executar(articleClient, empresa);
		});

		// Actualització de l'estat de la comanda cancel·lada
		actualitzarEstatComanda.executar(comanda.codi());

		return MissatgesCanvisComandaResponse.of(dif.missatges(), dif.missatgesServides());
	}

}
