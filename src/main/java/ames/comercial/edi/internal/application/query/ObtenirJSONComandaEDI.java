package ames.comercial.edi.internal.application.query;

import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObtenirJSONComandaEDI {

	@Autowired
	QueryRepository queryRepository;
//	@Autowired
//	ObtenirClientEDIAds obtenirClientEDIAds;

//	public ObtenirComandaEDI(ComandaEDIRepository comandaEDIRepository) {
//		this.comandaEDIRepository = comandaEDIRepository;
//	}

	public ComandaMissatgeEDI executar(Long codi) {
		return queryRepository.find(codi).get().json();
	}
	
}
