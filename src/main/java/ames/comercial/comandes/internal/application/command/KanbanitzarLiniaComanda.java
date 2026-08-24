package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.ComandesException.NoPotCanviarNomLiniaServida;
import ames.comercial.comandes.ComandesException.OperacioNoPermesaComandaStockSeguretat;
import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.request.KanbanitzarLiniaComandaRequest;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Empresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KanbanitzarLiniaComanda {

	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaComandaRepo;
	IObtenirNumeradorComanda obtenirNumeradorComanda;
	ActualitzarEstatComanda actualitzarEstatComanda;

	public KanbanitzarLiniaComanda(ComandaRepository comandaRepo, LiniaComandaRepository liniaComandaRepo,
                                   IObtenirNumeradorComanda obtenirNumeradorComanda, ActualitzarEstatComanda actualitzarEstatComanda) {
		this.comandaRepo = comandaRepo;
		this.liniaComandaRepo = liniaComandaRepo;
		this.obtenirNumeradorComanda = obtenirNumeradorComanda;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@Transactional
	public String executar (KeyLiniaComanda keyLinia, KanbanitzarLiniaComandaRequest req) {
		// Obtenció de la comanda a traspassar
		var comandaTraspassar = comandaRepo.find(keyLinia.comanda()).orElseThrow(ComandaNoExisteix::new);
		// No es pot traspassar comandes d'stock de seguretat
		if (comandaTraspassar.isStockSeguretat())
			throw new OperacioNoPermesaComandaStockSeguretat();
		// Obtenicó de la línia a kanbanitzar
		var liniaTraspassar = liniaComandaRepo.find(keyLinia.comanda(), keyLinia.numero()).orElseThrow(LiniaComandaNoExisteix::new);

		// No es pot fer el canvi d'una comanda que està servida
		if (liniaTraspassar.servida())
			throw new NoPotCanviarNomLiniaServida();

		// Es crea una comanda o s'afegeix a una si ja existeix prèviament
		var optComandaExistent = comandaRepo.findByComandaClient(comandaTraspassar.dades().client(), req.comanda(), Empresa.getByClau(comandaTraspassar.dades().empresa()));
		Comanda comanda = optComandaExistent.orElseGet(() -> buildNewComanda(comandaTraspassar, req));
		var codiComanda = comanda.codi();

		// S'anul· la línia
		var liniaAnulada = liniaTraspassar.anular();
		liniaComandaRepo.save(liniaAnulada);

		// Es crea la nova línia
		var newNumeroLinia = liniaComandaRepo.nextNumero(codiComanda);
		var liniaKanbanitzada = liniaTraspassar.traspas(KeyLiniaComanda.of(codiComanda, newNumeroLinia));
		liniaComandaRepo.save(liniaKanbanitzada);

		// Actualització de la comanda des d'on s'ha traspassat la línia
		// i també de la nova (o ja existent)
		actualitzarEstatComanda.executar(comandaTraspassar.codi());
		actualitzarEstatComanda.executar(comanda.codi());

		return liniaKanbanitzada.codiNumeroFormat();
	}
	
	private Comanda buildNewComanda(Comanda c, KanbanitzarLiniaComandaRequest req) {
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
	
	private InformacioClient buildInfoClient(KanbanitzarLiniaComandaRequest req) {
		return InformacioClientImpl.builder()
				.identificador(req.comanda())
				.data(RequestThread.dateLocal())
				.programa(req.programa())
				.build();
	}
	
}
