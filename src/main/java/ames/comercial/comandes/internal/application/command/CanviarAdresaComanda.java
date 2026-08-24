package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.request.CanviarAdresaComandaRequest;
import ames.comercial.costtransport.ICalcularCostTransport;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.AdresaImpl;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.InformacioEnviamentImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CanviarAdresaComanda {

	ComandaRepository comandaRepo;
	ICalcularCostTransport calcularCostTransport;

	public CanviarAdresaComanda(ComandaRepository comandaRepo, ApplicationEventPublisher applicationEventPublisher,
								ICalcularCostTransport calcularCostTransport) {
		this.comandaRepo = comandaRepo;
		this.calcularCostTransport = calcularCostTransport;
	}

	@Transactional
	public void executar (long idComanda, CanviarAdresaComandaRequest req) {
		Comanda comanda = comandaRepo.find(idComanda).orElseThrow(ComandaNoExisteix::new);
		comanda.canviarAdresa(buildAdresa(req));
		comanda.canviarInformacioEnviament(buildInfoEnviament(req));
		// En cas que es tracti d'una comanda de normalitzats cal actualitzar el cost de transport (es recalcula per si ha canviat l'Incoterm i el país)
		var optDadesNorm = comanda.dadesNormalitzat();
		if (optDadesNorm.isPresent()) {
			var dadesNorm = optDadesNorm.get();
			var costTransport = calcularCostTransport.calcula(comanda.dades().client(), req.pais(), dadesNorm.importNet(), dadesNorm.pes(), req.incoterm());
			comanda.canviarDadesNormalitzat(dadesNorm.actualitzarCostTransport(costTransport.orElse(BigDecimal.ZERO)));
		}
		// Update dels canvis a BBDD
		comandaRepo.save(comanda);
	}

	public Adresa buildAdresa(CanviarAdresaComandaRequest req) {
		return AdresaImpl.builder()
				.destinatari(req.destinatari())
				.adresa(req.adresa())
				.poblacio(req.poblacio())
				.codiPostal(req.codiPostal())
				.pais(req.pais())
				.build();
	}

	public InformacioEnviament buildInfoEnviament(CanviarAdresaComandaRequest req) {
		return InformacioEnviamentImpl.builder()
				.formaEnviament(req.formaEnviament())
				.incoterm(req.incoterm())
				.desti(req.desti().orElse(""))
				.transportista(req.transportista())
				.zonaTransport(req.zonaTransport())
				.build();
	}
	
}
