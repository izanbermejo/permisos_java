package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.OperacioNoPermesaComandaStockSeguretat;
import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.request.TraspassarComandaRequest;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Empresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TraspassarComanda {

	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaComandaRepo;
	IObtenirNumeradorComanda obtenirNumeradorComanda;
	ActualitzarEstatComanda actualitzarEstatComanda;

	public TraspassarComanda(ComandaRepository comandaRepo, LiniaComandaRepository liniaComandaRepo,
							 IObtenirNumeradorComanda obtenirNumeradorComanda, ActualitzarEstatComanda actualitzarEstatComanda) {
		this.comandaRepo = comandaRepo;
		this.liniaComandaRepo = liniaComandaRepo;
		this.obtenirNumeradorComanda = obtenirNumeradorComanda;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@Transactional
	public long executar (long codiComandaTraspassar, TraspassarComandaRequest req) {
		// Obtenció de la comanda a traspassar
		var comandaTraspassar = comandaRepo.find(codiComandaTraspassar).orElseThrow(ComandaNoExisteix::new);
		// No es pot traspassar una comanda d'stock de seguretat
		if (comandaTraspassar.isStockSeguretat())
			throw new OperacioNoPermesaComandaStockSeguretat();

		// Es crea una comanda o s'afegeix a una si ja existeix prèviament
		var optComandaExistent = comandaRepo.findByComandaClient(comandaTraspassar.dades().client(), req.comanda(), Empresa.getByClau(comandaTraspassar.dades().empresa()));
		Comanda comanda = optComandaExistent.orElseGet(() -> buildNewComanda(comandaTraspassar, req));
		var codiComanda = comanda.codi();

		// Obtenció de les línies pendents de la comanda a traspassar
		var liniesPendents = liniaComandaRepo.findByComanda(codiComandaTraspassar);
		// Filtre de les línies que s'han de traspassar
		var liniesTraspassar = liniesPendents.stream()
				.filter(l -> req.data().map(data -> !l.dataSolicitada().isBefore(data)).orElse(true))
				.toList();

		// Per cada línia traspassada s'ha de posar a 0 i crear una de nova traspassada a la nova comanda
		liniesTraspassar.forEach(l -> {
			var liniaAnulada = l.anular();
			liniaComandaRepo.save(liniaAnulada);
			var newNumeroLinia = liniaComandaRepo.nextNumero(codiComanda);
			var newLinia = l.traspas(KeyLiniaComanda.of(codiComanda, newNumeroLinia));
			liniaComandaRepo.save(newLinia);
		});

		// Actualització de l'estat de la comanda a traspassar
		// i de la nova comanda (per si ja existia)
		actualitzarEstatComanda.executar(comandaTraspassar.codi());
		actualitzarEstatComanda.executar(comanda.codi());

		return codiComanda;
	}
	
	private Comanda buildNewComanda(Comanda c, TraspassarComandaRequest req) {
		var newCodiComanda = obtenirNumeradorComanda.obtenir();
		ComandaProps props = ComandaPropsImpl.builder()
				.tipus(TipusComanda.PROGRAMA)
				.dades(buildDades(c.dades()))
				.informacioClient(buildInfoClient(req))
				.servida(false)
				.servible(Servible.NO_APLICA)
				.adresa(c.adresa())
				.informacioEnviament(c.informacioEnviament())
				.usuari(RequestThread.nomUsuari())
				.build();
		var comanda = new Comanda(newCodiComanda, props);
		comandaRepo.save(comanda);
		return comanda;
	}
	
	private DadesComanda buildDades(DadesComanda c) {
		return DadesComandaImpl.builder()
				.client(c.client())
				.clientNom(c.clientNom())
				.empresa(c.empresa())
				.dataAlta(RequestThread.dateLocal())
				.build();
	}
	
	private InformacioClient buildInfoClient(TraspassarComandaRequest req) {
		return InformacioClientImpl.builder()
				.identificador(req.comanda())
				.data(RequestThread.dateLocal())
				.programa(req.programa())
				.build();
	}
	
}
