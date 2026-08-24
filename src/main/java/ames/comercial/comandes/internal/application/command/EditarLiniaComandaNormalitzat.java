package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.events.ComandaNormalitzatModificadaEvent;
import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzatImpl;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.service.IRecalculReservesArticleclient;
import ames.comercial.comandes.internal.domain.service.ServeiEditarLiniaComandaNormalitzat;
import ames.comercial.comandes.internal.domain.service.ServeiEditarLiniaComandaNormalitzatRequestImpl;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.service.CalculCanvisLiniesComanda;
import ames.comercial.comandes.service.CalculadoraComandaNormalitzat;
import ames.comercial.comandes.service.IProviderDiesReserva;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.LiniaNormalitzatReqImpl;
import ames.comercial.comandes.service.request.TarifesCalculadoraReq;
import ames.comercial.comandes.service.response.MissatgesCanvisComandaResponse;
import ames.comercial.shared.Empresa;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class EditarLiniaComandaNormalitzat {

	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	CalculadoraComandaNormalitzat calculadoraComandaNormalitzat;
	IRecalculReservesArticleclient recalculReservesArticleclient;
	IProviderDiesReserva providerDiesReserva;

	public EditarLiniaComandaNormalitzat(ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
                                         LiniaComandaRepository liniaRepo, CalculadoraComandaNormalitzat calculadoraComandaNormalitzat,
                                         IRecalculReservesArticleclient recalculReservesArticleclient,
										 IProviderDiesReserva providerDiesReserva) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.calculadoraComandaNormalitzat = calculadoraComandaNormalitzat;
		this.recalculReservesArticleclient = recalculReservesArticleclient;
		this.providerDiesReserva = providerDiesReserva;
	}

	@Transactional
	public MissatgesCanvisComandaResponse executar (KeyLiniaComanda clau, LiniaNormalitzatReq req) {
		// La comanda ha d'existir
		var comanda = comandaRepo.find(clau.comanda()).orElseThrow(ComandaNoExisteix::new);
		// Obtenció de les línies de la comanda
		var linies = liniaRepo.findByComanda(clau.comanda());
		// Obtenció de les dades de càlcul de normalitzat
		var dadesNorm = comanda.dadesNormalitzat().orElseThrow();
		var tarifesCalc = dadesNorm.tarifes();
		// Línia a editar
		var linia = linies.stream().filter(l -> l.id().equals(clau)).findAny().orElseThrow(ComandaNoExisteix::new);
		// Articleclient
		var articleClient = linia.articleClient();
		// Empresa
		var empresa = Empresa.getByClau(comanda.dades().empresa());

		// Calculadora de preus amb la línia editada (cal indicar si ja té el preu fixat)
		var reqNorm = LiniaNormalitzatReqImpl.builder().from(req)
				.linia(linia.id().numero())
				.preuFixat(linia.isPreuFixat() ? Optional.of(linia.preu()) : Optional.empty())
				.build();
		var reqCalc = Stream.concat(linies.stream()
						.filter(l -> !l.id().equals(linia.id()))
						.map(l -> LiniaNormalitzatReq.of(l, false)),
				Stream.of(reqNorm)).toList();
		var respCalc = calculadoraComandaNormalitzat.calcula(comanda.dades().client(), TarifesCalculadoraReq.from(tarifesCalc), reqCalc);

		// Càlcul de diferències
		var dif = new CalculCanvisLiniesComanda(linies, respCalc).calcula();

		// Edició de la línia amb el canvi de quantitat i de dates
		var infoNewLinia = respCalc.linies().stream().filter(l -> l.linia() == linia.id().numero()).findAny().orElseThrow();
		var serveiEdicioLinia = new ServeiEditarLiniaComandaNormalitzat(providerDiesReserva, linia);
		var liniaUpdated = serveiEdicioLinia.executar(ServeiEditarLiniaComandaNormalitzatRequestImpl.builder()
				.novaQuantitat(req.quantitat())
				.quantitatReservableDisponible(infoNewLinia.quantitatReservable())
				.nouPreu(infoNewLinia.preu())
				.quantitatCalcul(infoNewLinia.quantitatCalcul())
				.dataSolicitada(req.dataSolicitada())
				.dataPrevistaSortida(req.dataPrevistaSortida())
				.build());

		// Només es guarden els canvis en cas que hagi canviat la línia
		if (liniaUpdated.equals(linia))
			return MissatgesCanvisComandaResponse.of(dif.missatges(), dif.missatgesServides());

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

		// Recàlcul reserves (sempre que s'hagi canviat la quantitat)
		var diferenciaStockReservat = LiniaComanda.diferenciaStockReservat(linia, liniaUpdated);
		if (diferenciaStockReservat != 0) {
			recalculReservesArticleclient.executar(articleClient, empresa);
		}

		var liniesActualitzar = Stream.of(List.of(liniaUpdated),
					liniesCanviPreu)
				.flatMap(List::stream).toList();
		applicationEventPublisher.publishEvent(new ComandaNormalitzatModificadaEvent(this, comanda, liniesActualitzar, articleClient, diferenciaStockReservat));

		return MissatgesCanvisComandaResponse.of(dif.missatges(), dif.missatgesServides());
	}

}
