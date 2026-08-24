package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.events.ComandaNormalitzatModificadaEvent;
import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzatImpl;
import ames.comercial.comandes.internal.domain.linia.*;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.service.CalculCanvisLiniesComanda;
import ames.comercial.comandes.service.CalculadoraComandaNormalitzat;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.LiniaNormalitzatReqImpl;
import ames.comercial.comandes.service.request.TarifesCalculadoraReq;
import ames.comercial.comandes.service.response.MissatgesCanvisComandaResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
public class AfegirLiniaComandaNormalitzat {

	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	CalculadoraComandaNormalitzat calculadoraComandaNormalitzat;

	public AfegirLiniaComandaNormalitzat(ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
                                         LiniaComandaRepository liniaRepo, CalculadoraComandaNormalitzat calculadoraComandaNormalitzat) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.calculadoraComandaNormalitzat = calculadoraComandaNormalitzat;
	}

	@Transactional
	public MissatgesCanvisComandaResponse executar (long codiComanda, LiniaNormalitzatReq req) {
		// La comanda ha d'existir
		var comanda = comandaRepo.find(codiComanda).orElseThrow(ComandaNoExisteix::new);
		// Obtenció de les línies de la comanda
		var linies = liniaRepo.findByComanda(codiComanda);
		// Obtenció de les dades de càlcul de normalitzat
		var dadesNorm = comanda.dadesNormalitzat().orElseThrow();
		var tarifesCalc = dadesNorm.tarifes();
		// Càlcul del nou id de la línia
		var newIdLinia = liniaRepo.nextNumero(codiComanda);
		var newKeyLinia = KeyLiniaComanda.of(codiComanda, newIdLinia);

		// Calculadora de preus amb la nova línia (les ja existents no es calcula la informació de reserva
		// ja que ja ho estan)
		var reqNorm = LiniaNormalitzatReqImpl.builder().from(req).linia(newIdLinia).build();
		var reqCalc = Stream.concat(linies.stream().map(l -> LiniaNormalitzatReq.of(l, false)), Stream.of(reqNorm)).toList();
		var respCalc = calculadoraComandaNormalitzat.calcula(comanda.dades().client(), TarifesCalculadoraReq.from(tarifesCalc), reqCalc);

		// Càlcul de diferències
		var dif = new CalculCanvisLiniesComanda(linies, respCalc).calcula();

		// Creació de la nova línia
		var infoNewLinia = respCalc.linies().stream().filter(l -> l.linia() == newIdLinia).findAny().orElseThrow();

		// Construcció de la nova línia amb el preu i reserva calculat
		var newLinia = LiniaComandaImpl.builder()
				.id(newKeyLinia)
				.articleClient(req.articleClient())
				.referencia(infoNewLinia.referencia())
				.tipusArticleClient(infoNewLinia.tipusArticleClient())
				.tipus(TipusLiniaComanda.FERM)
				.quantitat(req.quantitat())
				.preu(infoNewLinia.preu())
				.dataCreacio(LocalDateTime.now())
				.dataSolicitada(req.dataSolicitada())
				.dataPrevistaSortida(req.dataPrevistaSortida())
				.quantitatServida(0L)
				.reserva(InformacioReservaImpl.builder()
						.estat(infoNewLinia.reservable())
						.quantitat(infoNewLinia.quantitatReservable())
						.build())
				.dadesCalcul(DadesCalculNormalitzatImpl.builder()
						.descompte(infoNewLinia.descompte())
						.quantitatCalcul(infoNewLinia.quantitatCalcul())
						.build())
				.build();

		// Es guarda la nova línia i les que han canviat de preu
		var liniesGuardar = Stream.concat(Stream.of(newLinia),
				dif.liniesCanviPreu().stream()).toList();
		liniaRepo.save(liniesGuardar);

		// Actualització de les dades de càlcul normalitzades (excepte la divisa que no canvia)
		comanda.canviarDadesNormalitzat(DadesNormalitzatImpl.builder()
						.from(dadesNorm)
						.importNet(respCalc.importNet())
						.importBrut(respCalc.importBrut())
						.pes(respCalc.pes())
						.costTransport(respCalc.costTransport().orElse(BigDecimal.ZERO))
						.build());
		comandaRepo.save(comanda);

		// Modificació de la comanda de normalitzats
		applicationEventPublisher.publishEvent(new ComandaNormalitzatModificadaEvent(this, comanda, liniesGuardar,
				req.articleClient(), infoNewLinia.quantitatReservable()));

		return MissatgesCanvisComandaResponse.of(dif.missatges(), dif.missatgesServides());
	}

}
