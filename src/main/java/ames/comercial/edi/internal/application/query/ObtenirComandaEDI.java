package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSONImpl;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirComandaEDI {

	@Autowired
	QueryRepository queryRepository;
//	@Autowired
//	ObtenirClientEDIAds obtenirClientEDIAds;

//	public ObtenirComandaEDI(ComandaEDIRepository comandaEDIRepository) {
//		this.comandaEDIRepository = comandaEDIRepository;
//	}

	public Optional<DadesComandaEDI> executar(Long codi) {
		return queryRepository.find(codi);
	}
	
}
