package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.internal.ObtenirArticleClientEDIAds;
import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.advantage.internal.response.QueryArticleClientEDIResponse;
import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.internal.application.command.ImportaFitxersEntrada;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSONImpl;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.shared.KeyArticleAmes;
import ames.comercial.shared.ValidationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirComandesEDI {

	@Autowired
	ComandaEDIRepository comandaEDIRepository;
	@Autowired
	ObtenirClientEDIAds obtenirClientEDIAds;
	@Autowired
	ObtenirLiniesComandesEDI obtenirLiniesComandesEDI;
	@Autowired
	ObtenirArticleClientEDIAds obtenirArticleClientEDIAds;
	@Autowired
	QueryRepository queryRepository;
	@Autowired
	ImportaFitxersEntrada importaFitxersEntrada;

	static final Logger log = LogManager.getLogger(ImportaFitxersEntrada.class.getName());

	public ObtenirComandesEDI(ComandaEDIRepository comandaEDIRepository) {
		this.comandaEDIRepository = comandaEDIRepository;
	}

	public List<DadesComandaEDINoJSON> executar() {
		return executar(null);
	}

	public List<DadesComandaEDINoJSON> executar(String usuari) {

		List<DadesComandaEDINoJSON> orders = queryRepository.list(usuari);
		List<DadesComandaEDINoJSON> ordersWithFlags = new ArrayList<DadesComandaEDINoJSON>();
		DadesComandaEDINoJSONImpl.Builder builder = DadesComandaEDINoJSONImpl.builder();
		for (DadesComandaEDINoJSON order : orders) { //TODO temporal, quan estiguin aquestes dades al postgres
			// TODO llavors farem un join per poder tenir aquestes dades també
			String document = "";

			Boolean orderFixed=false;

			ObtenirClientEDIAds.ClientEDIAds clientEDIProfile =null;
			if (order.clientProfile()!=null && !order.clientProfile().isEmpty())
				clientEDIProfile=order.clientProfile().get();
			else {
				// reintento carregar a BBDD el perfil EDI del client
				Optional<ObtenirClientEDIAds.ClientEDIAds> clientEDIAdsFlags=Optional.empty();
				if (order.document().equals("DELFOR"))
					clientEDIAdsFlags = obtenirClientEDIAds.get(order.codiClientAmes(),"DELF","");
				else if (order.document().equals("DELJIT"))
					clientEDIAdsFlags = obtenirClientEDIAds.get(order.codiClientAmes(),"DELJ","");
				if (clientEDIAdsFlags!=null && !clientEDIAdsFlags.isEmpty())
					comandaEDIRepository.updateClientProfile(order.codi().get(),clientEDIAdsFlags.get());
			}

			if (order.status().equals(DadesComandaEDI.ENUM_STATUS_ERROR_NAD_NOTFOUND) || order.status().equals(DadesComandaEDI.ENUM_STATUS_ERROR_NAD_DUPLICATED)){
				DadesComandaEDI orderToFix = queryRepository.find(order.codi().get()).get();
				ComandaMissatgeEDI pedido = orderToFix.json();
				List<QueryArticleClientEDIResponse> clientsComandaEDI = obtenirArticleClientEDIAds.findByNADAndArtRef(order.nad(), pedido.getLineas().get(0).getLA().getIdArticuloComprador());
				// Nomes reimporta la comanda si ara si troba un unic client per la comanda EDI amb error previ
				if (clientsComandaEDI.size()==1) {
					log.info("Reimportem EDI amb codi de comanda=" + orderToFix.codi() + " per que tenia el nad=" + orderToFix.nad() + " duplicat.");
					ValidationResult validationResult = importaFitxersEntrada.validate(pedido);
					importaFitxersEntrada.saveOrderToDatabase(pedido, new File(orderToFix.pathEDI()), validationResult);
					comandaEDIRepository.delete(orderToFix.codi().get());
				}
//				List<Linea> linies = orderToFix.json().getLineas();
////				List<DadesLiniaEDI> linies = obtenirLiniesComandesEDI.listLinesEDI(orderToFix.codi().get());
//				for (Linea linea : linies) {
//					List<QueryArticleClientEDIResponse> artCliAdsList = obtenirArticleClientEDIAds.findByNADAndArtRef(orderToFix.nad(), linea.getLA().getIdArticuloComprador());
//					if (artCliAdsList.size()==1) {
//						if (!orderFixed) {
//							comandaEDIRepository.fixComandaWithNADIssue(orderToFix.codi().get(), artCliAdsList.get(0).clicod(), artCliAdsList.get(0).nomClient().get());
//							orderFixed=true;
//						}
//						List<DadesLiniaEDI> liniesBBDD = importaFitxersEntrada.parseDadesLiniaEDI();
//						comandaEDIRepository.fixLiniaWithNADIssue(linia.codi().get(),artCliAdsList.get(0).artInt(),artCliAdsList.get(0).referencia());
//					}
//				}

//				comandaEDIRepository.fixNADIssue(orderToFix);
			}

			builder.codi(order.codi())
					.pathPDF(order.pathPDF())
					.numeroEnviament(order.numeroEnviament())
					.missatgeTipus(order.missatgeTipus())
					.missatgeNumero(order.missatgeNumero())
					.data(order.data())
					.referencia(order.referencia())
					.pathEDI(order.pathEDI())
					.document(order.document())
					.observacions(order.observacions())
					.usuariLogistica(order.usuariLogistica())
					.codiClientAmes(order.codiClientAmes())
					.nomClientAmes(order.nomClientAmes())
					.deleted(order.deleted())
					.insertedAt(order.insertedAt())
					.insertedBy(order.insertedBy())
					.status(order.status())
					.nad(order.nad())
					.clientProfile(order.clientProfile())
					.nadPath(order.nadPath())
					.tipus(order.tipus())
					.bustia(order.bustia())
			;

			List<KeyArticleAmes> articles  = queryRepository.listArticles(order.codi().get());

			if (articles.size()>1)
				builder.article("M");
			else if (articles.size()==1)
				builder.article(articles.get(0).codiArticleAmes());
			else
				builder.article("-");
			ordersWithFlags.add(builder.build());



//            ordersWithFlags.add()
		}

		ordersWithFlags.sort(Comparator
				.comparing(DadesComandaEDINoJSON::tipus, Comparator.reverseOrder()) // Descendente
				.thenComparing(DadesComandaEDINoJSON::missatgeNumero)
		);

		return ordersWithFlags;
	}
	
}
