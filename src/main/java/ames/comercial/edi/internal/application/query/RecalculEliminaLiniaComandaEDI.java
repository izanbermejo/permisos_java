package ames.comercial.edi.internal.application.query;

import ames.comercial.edi.response.ComandaEDIResponse;
import ames.comercial.edi.response.ComandaEDIResponseImpl;
import ames.comercial.edi.response.LiniaEDIResponse;
import ames.comercial.edi.response.LiniaEDIResponseImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecalculEliminaLiniaComandaEDI {

//	@Autowired
//	ComandaEDIRepository comandaEDIRepository;

	public RecalculEliminaLiniaComandaEDI() {
//		this.comandaEDIRepository = comandaEDIRepository;
	}

	public ComandaEDIResponse executar(Long codi, ComandaEDIResponse comanda) {
		ComandaEDIResponse outComandaEDIResponse;
		ComandaEDIResponseImpl.Builder outComandaEDIResponseBuilder = ComandaEDIResponseImpl.builder();
		List<LiniaEDIResponse> liniesRes = new ArrayList<LiniaEDIResponse>();
		LiniaEDIResponseImpl.Builder liniaEDIResponseBuilder = LiniaEDIResponseImpl.builder();
		List<LiniaEDIResponse> liniesReq = comanda.linies().get();
		int quantitatAcumulada = 0;

		outComandaEDIResponseBuilder
				.nomClientAmes(comanda.nomClientAmes())
				.codiArticle(comanda.codiArticle())
				.codiArticleFab(comanda.codiArticleFab())
				.pathPDF(comanda.pathPDF())
				.codiArticleAmes(comanda.codiArticleAmes())
				.missatgeNumero(comanda.missatgeNumero())
				.acumulatArticle(comanda.acumulatArticle());

		for (LiniaEDIResponse liniaEDIResponse : liniesReq) {
			quantitatAcumulada = quantitatAcumulada + liniaEDIResponse.quantitat();
			if (liniaEDIResponse.codi()!=null && !liniaEDIResponse.codi().get().equals(codi)) {
				liniaEDIResponseBuilder.codi(liniaEDIResponse.codi())
						.codiComanda(liniaEDIResponse.codiComanda())
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
						.codiComandaClient(liniaEDIResponse.codiComandaClient())
						.codiComandaClientProcessat(liniaEDIResponse.codiComandaClientProcessat())
						.processable(liniaEDIResponse.processable());
				liniesRes.add(liniaEDIResponseBuilder.build());
			} else
				quantitatAcumulada = quantitatAcumulada - liniaEDIResponse.quantitat();
		}

		outComandaEDIResponseBuilder.comanda(comanda.comanda());

		outComandaEDIResponseBuilder.linies(Optional.of(liniesRes));

		return outComandaEDIResponseBuilder.build();
	}
	
}
