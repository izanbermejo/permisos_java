package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.CanviarPreuLiniaServida;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.events.ComandaNormalitzatModificadaEvent;
import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzatImpl;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.service.CalculadoraComandaNormalitzat;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.TarifesCalculadoraReq;
import ames.comercial.shared.Preu;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FixarPreuLiniaComandaNormalitzat {

	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	CalculadoraComandaNormalitzat calculadoraComandaNormalitzat;

	public FixarPreuLiniaComandaNormalitzat(ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
                                            LiniaComandaRepository liniaRepo, CalculadoraComandaNormalitzat calculadoraComandaNormalitzat) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.calculadoraComandaNormalitzat = calculadoraComandaNormalitzat;
	}

	/**
	 * Fixa el preu d'una línia de comanda. Es fa un càlcul de la comanda amb la línia fixada
	 * i s'actualitza la línia i la comanda
	 *
	 * @param clau Clau de la línia de comanda a fixar el preu
	 * @param preu Preu a fixar
	 */
	@Transactional
	public void executar (KeyLiniaComanda clau, BigDecimal preu) {
		// La comanda ha d'existir
		var comanda = comandaRepo.find(clau.comanda()).orElseThrow(ComandaNoExisteix::new);
		// Obtenció de les línies de la comanda
		var linies = liniaRepo.findByComanda(clau.comanda());
		// Obtenció de les dades de càlcul de normalitzat
		var dadesNorm = comanda.dadesNormalitzat().orElseThrow();
		var tarifesCalc = dadesNorm.tarifes();
		// Línia a editar
		var linia = linies.stream().filter(l -> l.id().equals(clau)).findAny().orElseThrow(ComandaNoExisteix::new);
		// En cas que s'hagi servit alguna peça ja no es podrà fixar el preu
		if (linia.quantitatServida() > 0)
			throw new CanviarPreuLiniaServida();
		// Articleclient
		var articleClient = linia.articleClient();

		// Línia amb el preu fixat
		var liniaPreuFixat = linia.fixarPreu(Preu.of(preu, linia.preu().divisa()));

		// Calculadora de preus amb la línia amb el preu fixat
		var reqNorm = LiniaNormalitzatReq.of(liniaPreuFixat, false);
		var reqCalc = Stream.concat(linies.stream()
						.filter(l -> !l.id().equals(linia.id()))
						.map(l -> LiniaNormalitzatReq.of(l, false)),
				Stream.of(reqNorm)).toList();
		var respCalc = calculadoraComandaNormalitzat.calcula(comanda.dades().client(), TarifesCalculadoraReq.from(tarifesCalc), reqCalc);

		// Es guarda la nova línia amb el preu fixat
		liniaRepo.save(liniaPreuFixat);

		// Actualització de les dades de càlcul normalitzades (excepte la divisa que no canvia)
		comanda.canviarDadesNormalitzat(DadesNormalitzatImpl.builder()
						.from(dadesNorm)
						.importNet(respCalc.importNet())
						.importBrut(respCalc.importBrut())
						.pes(respCalc.pes())
						.costTransport(respCalc.costTransport().orElse(BigDecimal.ZERO))
						.build());
		comandaRepo.save(comanda);

		var liniesActualitzar = List.of(liniaPreuFixat);
		applicationEventPublisher.publishEvent(new ComandaNormalitzatModificadaEvent(this, comanda, liniesActualitzar));
	}

}
