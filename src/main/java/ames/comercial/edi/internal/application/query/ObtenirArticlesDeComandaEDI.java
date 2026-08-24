package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSONImpl;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.shared.KeyArticleAmes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirArticlesDeComandaEDI {

	@Autowired
	QueryRepository queryRepository;
//	@Autowired
//	ObtenirClientEDIAds obtenirClientEDIAds;

//	public ObtenirArticlesDeComandaEDI(ComandaEDIRepository comandaEDIRepository) {
//		this.queryRepository = queryRepository;
//	}

	public ObtenirArticlesDeComandaEDI() {
//		this.queryRepository = queryRepository;
	}

	public List<KeyArticleAmes> executar(Long codi_comanda) {
		if (codi_comanda==null || queryRepository.find(codi_comanda).isEmpty())
				return null;
		DadesComandaEDI comanda = queryRepository.find(codi_comanda).get();
		ObtenirClientEDIAds.ClientEDIAds flags = comanda.clientProfile().get();
		List<KeyArticleAmes> list = queryRepository.listArticles(codi_comanda,flags.diestall());
		return list;
	}
	
}
