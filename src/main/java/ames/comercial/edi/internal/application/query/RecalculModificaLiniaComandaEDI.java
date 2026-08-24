package ames.comercial.edi.internal.application.query;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi.request.LiniaArticleEditRequest;
import ames.comercial.edi.response.ComandaEDIResponse;
import ames.comercial.edi.response.ComandaEDIResponseImpl;
import ames.comercial.edi.response.LiniaEDIResponse;
import ames.comercial.edi.response.LiniaEDIResponseImpl;
import ames.comercial.shared.Dates;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecalculModificaLiniaComandaEDI {

//	@Autowired
//	ComandaEDIRepository comandaEDIRepository;

	public RecalculModificaLiniaComandaEDI() {
//		this.comandaEDIRepository = comandaEDIRepository;
	}

	public ComandaEDIResponse executar(Long codi, Long codiLiniaProcessat, LiniaArticleEditRequest request) {
//		ComandaEDIResponse outComandaEDIResponse;
		ComandaEDIResponseImpl.Builder outComandaEDIResponseBuilder = ComandaEDIResponseImpl.builder();
		List<LiniaEDIResponse> liniesRes = new ArrayList<LiniaEDIResponse>();
		LiniaEDIResponseImpl.Builder liniaEDIResponseBuilder = LiniaEDIResponseImpl.builder();
		List<LiniaEDIResponse> liniesReq = request.comanda().linies().get();
		int quantitatAcumulada = 0;

		outComandaEDIResponseBuilder
				.nomClientAmes(request.comanda().nomClientAmes())
				.codiArticle(request.comanda().codiArticle())
				.codiArticleAmes(request.comanda().codiArticleAmes())
				.missatgeNumero(request.comanda().missatgeNumero())
				.comanda(request.comanda().comanda())
				.stockArticle(request.comanda().stockArticle());

		for (LiniaEDIResponse liniaEDIResponse : liniesReq) {

			if ((!liniaEDIResponse.codi().isEmpty() && !liniaEDIResponse.codi().get().equals(codi)) || (!liniaEDIResponse.codiLiniaProcessat().isEmpty() && !liniaEDIResponse.codiLiniaProcessat().get().equals(codiLiniaProcessat))) {
				quantitatAcumulada = quantitatAcumulada + liniaEDIResponse.quantitat();
				liniaEDIResponseBuilder.codi(liniaEDIResponse.codi())
						.codiComanda(liniaEDIResponse.codiComanda())
						.codiComandaProcessat(liniaEDIResponse.codiComandaProcessat())
						.codiComandaClientProcessat(liniaEDIResponse.codiComandaClientProcessat())
						.codiComandaClient(liniaEDIResponse.codiComandaClient())
						.dataClient(liniaEDIResponse.dataClient())
						.dataAMES(liniaEDIResponse.dataAMES())
						.dataMagatzem(liniaEDIResponse.dataMagatzem())
						.quantitat(liniaEDIResponse.quantitat())
						.quantitatProcessat(liniaEDIResponse.quantitatProcessat())
						.quantitatPendent(liniaEDIResponse.quantitatPendent())
						.quantitatAcumulada(quantitatAcumulada) // recalculem la quantitat acumulada
						.tipus(liniaEDIResponse.tipus())
						.observacions(liniaEDIResponse.observacions())
						.comentarisAMES(liniaEDIResponse.comentarisAMES())
						.alertesAMES(liniaEDIResponse.alertesAMES())
						.ultimAlbara(liniaEDIResponse.ultimAlbara())
						.status(liniaEDIResponse.status())
						.codiArticle(liniaEDIResponse.codiArticle())
						.codiArticleAmes(liniaEDIResponse.codiArticleAmes())
						.processable(liniaEDIResponse.processable())
						.codiLiniaProcessat(liniaEDIResponse.codiLiniaProcessat());
				liniesRes.add(liniaEDIResponseBuilder.build());
			} else {
				quantitatAcumulada = quantitatAcumulada + request.quantitat();
				liniaEDIResponseBuilder.codi(codi)
						.codiComanda(liniaEDIResponse.codiComanda())
						.codiComandaClient(liniaEDIResponse.codiComandaClient())
						.codiComandaClientProcessat(liniaEDIResponse.codiComandaClientProcessat())
						.dataClient(Dates.convertToSQLDate(request.dataClient()))
						.dataAMES(Dates.convertToSQLDate(request.dataAmes()))
						.codiLiniaProcessat(liniaEDIResponse.codiLiniaProcessat()).codiComandaProcessat(liniaEDIResponse.codiComandaProcessat());
//				if (!request.dataMagatzem().isEmpty())
//					liniaEDIResponseBuilder.dataMagatzem(Dates.convertToSQLDate(request.dataMagatzem().get()));
				liniaEDIResponseBuilder.quantitat(request.quantitat())
						.quantitatProcessat(liniaEDIResponse.quantitatProcessat())
						.quantitatPendent(liniaEDIResponse.quantitatPendent())
						.quantitatAcumulada(quantitatAcumulada); // recalculem la quantitat acumulada amb la nova quantitat editada
				TipusLiniaComanda tipus=TipusLiniaComanda.FERM;
				if (request.tipus().equals("FERM"))
						tipus=TipusLiniaComanda.FERM;
				else if (request.tipus().equals("ORIENTATIU"))
					tipus=TipusLiniaComanda.ORIENTATIU;
				else if (request.tipus().equals("INVENT"))
					tipus=TipusLiniaComanda.INVENT;
					liniaEDIResponseBuilder.tipus(tipus)
						.observacions(liniaEDIResponse.observacions())
						.comentarisAMES(liniaEDIResponse.comentarisAMES())
						.alertesAMES(liniaEDIResponse.alertesAMES())
						.ultimAlbara(liniaEDIResponse.ultimAlbara())
						.status(liniaEDIResponse.status())
						.codiArticle(liniaEDIResponse.codiArticle())
						.codiArticleAmes(liniaEDIResponse.codiArticleAmes())
						.processable(true);
				liniesRes.add(liniaEDIResponseBuilder.build());
			}
		}

		outComandaEDIResponseBuilder.linies(liniesRes);

		return outComandaEDIResponseBuilder.build();
	}

}