package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.events.ComandaNormalitzatModificadaEvent;
import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzatImpl;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class EliminarLiniaComandaNormalitzat {

	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	CalculadoraComandaNormalitzat calculadoraComandaNormalitzat;
	IRecalculReservesArticleclient recalculReservesArticleclient;

	public EliminarLiniaComandaNormalitzat(ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
                                           LiniaComandaRepository liniaRepo, CalculadoraComandaNormalitzat calculadoraComandaNormalitzat,
										   IRecalculReservesArticleclient recalculReservesArticleclient) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.calculadoraComandaNormalitzat = calculadoraComandaNormalitzat;
		this.recalculReservesArticleclient = recalculReservesArticleclient;
	}

	@Transactional
	public MissatgesCanvisComandaResponse executar (KeyLiniaComanda clau) {
		// La comanda ha d'existir
		var comanda = comandaRepo.find(clau.comanda()).orElseThrow(ComandaNoExisteix::new);
		// Obtenció de les línies de la comanda
		var linies = liniaRepo.findByComanda(clau.comanda());
		// Obtenció de les dades de càlcul de normalitzat
		var dadesNorm = comanda.dadesNormalitzat().orElseThrow();
		var tarifesCalc = dadesNorm.tarifes();
		// Línia a eliminar
		var linia = linies.stream().filter(l -> l.id().equals(clau)).findAny().orElseThrow(ComandaNoExisteix::new);
		// Articleclient
		var articleClient = linia.articleClient();
		// Empresa
		var empresa = Empresa.getByClau(comanda.dades().empresa());

		// Calculadora de preus sense la línia a eliminar
		var reqCalc = linies.stream()
				.filter(l -> !l.id().equals(clau))
				.map(LiniaNormalitzatReq::of).toList();
		var respCalc = calculadoraComandaNormalitzat.calcula(comanda.dades().client(), TarifesCalculadoraReq.from(tarifesCalc), reqCalc);

		// Càlcul de diferències
		var dif = new CalculCanvisLiniesComanda(linies, respCalc).calcula();

		// Actualització de la línia a eliminar (eliminar info reservas i quantitat a 0)
		var liniaUpdated = linia.eliminar();

		// Es guarda la nova línia
		liniaRepo.save(liniaUpdated);
		// Es guarda les línies de la comanda que han canviat de preu
		var liniesCanviPreu = dif.liniesCanviPreu().stream()
				.filter(l -> l.numero() != linia.numero())
				.toList();
		liniaRepo.save(liniesCanviPreu);

		// Actualització de les dades de càlcul normalitzades (excepte la divisa que no canvia)
		comanda.canviarDadesNormalitzat(DadesNormalitzatImpl.builder()
				.from(dadesNorm)
				.importNet(respCalc.importNet())
				.importBrut(respCalc.importBrut())
				.pes(respCalc.pes())
				.costTransport(respCalc.costTransport().orElse(BigDecimal.ZERO))
				.build());
		comandaRepo.save(comanda);

		// Recàlcul reserves (només en cas que s'hagi decrementat la quantitat)
		var diferenciaStockReservat = LiniaComanda.diferenciaStockReservat(linia, liniaUpdated);
		var liniesCanviReserva = new ArrayList<LiniaComanda>();
		if (diferenciaStockReservat < 0) {
			recalculReservesArticleclient.executar(articleClient, empresa);
		}
		liniaRepo.save(liniesCanviReserva);

		var liniesActualitzar = Stream.of(List.of(liniaUpdated),
						liniesCanviPreu,
						liniesCanviReserva)
				.flatMap(List::stream).toList();
		applicationEventPublisher.publishEvent(new ComandaNormalitzatModificadaEvent(this, comanda, liniesActualitzar));

		return MissatgesCanvisComandaResponse.of(dif.missatges(), dif.missatgesServides());
	}

}
