package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.internal.ObtenirAlbaransFacturesByArticleAndClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirDadesAcumulatArticlesAds;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByArticleAndClientResponse;
import ames.comercial.shared.KeyArticleClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObtenirAlbaransEDI {

	@Autowired
	ObtenirAlbaransFacturesByArticleAndClientAds obtenirAlbaransFacturesByArticleAndClientAds;
	@Autowired
	ObtenirClientAds obtenirClientAds;
	@Autowired
	ObtenirDadesAcumulatArticlesAds obtenirDadesAcumulatArticlesAds;

	public ObtenirAlbaransEDI(ObtenirAlbaransFacturesByArticleAndClientAds obtenirAlbaransFacturesByArticleAndClientAds,
							  ObtenirClientAds obtenirClientAds) {
		this.obtenirAlbaransFacturesByArticleAndClientAds = obtenirAlbaransFacturesByArticleAndClientAds;
		this.obtenirClientAds = obtenirClientAds;
	}

	public List<QueryAlbaransFacturesByArticleAndClientResponse> executar(String codiArticle, String codiClient, String ultimAlbaraRebut,String tipusClient) {
//		obtenirClientAds.get(codiClient).get().
		String dataAcumulat; String stockAcumulat; String albaraAcumulat;

		KeyArticleClientImpl.Builder articleClientBuilder = KeyArticleClientImpl.builder();
		articleClientBuilder.artint(codiArticle).clicod(codiClient);
		Optional<ObtenirDadesAcumulatArticlesAds.DadesAcumulatArticle> dadesAcumulats = obtenirDadesAcumulatArticlesAds.query(articleClientBuilder.build());


//		if (dadesAcumulats !=null && !dadesAcumulats.isEmpty())
//			dataAcumulat = dadesAcumulats.get().dataAcumulat();
//		if (albaraAcumulat!=null && !albaraAcumulat.isEmpty())
//			albaraAcumulat = dadesAcumulats.get().albaraAcumulat();
//		if (stockAcumulat!=null && !stockAcumulat.isEmpty())
//			stockAcumulat = dadesAcumulats.get().stockAcumulat();

//		dataAcumulat = null;
//		if (!dadesAcumulats.isEmpty())
		dataAcumulat = dadesAcumulats.get().dataAcumulat();
		albaraAcumulat = dadesAcumulats.get().albaraAcumulat();
		stockAcumulat = dadesAcumulats.get().stockAcumulat();

		Optional<ObtenirClientAds.ClientAds> client = obtenirClientAds.get(codiClient);
		String enviamentClient = client.get().formaEnviament().codiAdvantage() + client.get().incoterm() + client.get().desti();
		if (ultimAlbaraRebut==null || ultimAlbaraRebut.equals("") || ultimAlbaraRebut.equals("undefined")  || Character.isDigit(ultimAlbaraRebut.charAt(0)))
			return obtenirAlbaransFacturesByArticleAndClientAds.queryAlbara(codiArticle, client.get().empresa(), codiClient, ultimAlbaraRebut,(tipusClient.equals("1")),dataAcumulat,stockAcumulat,albaraAcumulat,enviamentClient);
		else {
			return obtenirAlbaransFacturesByArticleAndClientAds.queryFactura(client.get().empresa(), codiArticle, codiClient, ultimAlbaraRebut,dataAcumulat,stockAcumulat,albaraAcumulat,enviamentClient);
		}
	}
	
}
