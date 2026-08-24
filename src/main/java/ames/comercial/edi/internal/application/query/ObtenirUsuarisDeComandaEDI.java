package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.shared.KeyArticleAmes;
import ames.comercial.shared.Usuari;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenirUsuarisDeComandaEDI {

	@Autowired
	QueryRepository queryRepository;
//	@Autowired
//	ObtenirClientEDIAds obtenirClientEDIAds;

//	public ObtenirArticlesDeComandaEDI(ComandaEDIRepository comandaEDIRepository) {
//		this.queryRepository = queryRepository;
//	}

	public ObtenirUsuarisDeComandaEDI() {
//		this.queryRepository = queryRepository;
	}

	public List<Usuari> executar() {
		List<Usuari> list = queryRepository.listUsuaris();
		return list;
	}
	
}
