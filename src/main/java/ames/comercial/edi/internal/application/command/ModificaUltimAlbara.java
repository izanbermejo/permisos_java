package ames.comercial.edi.internal.application.command;

import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModificaUltimAlbara {

	@Autowired
	ComandaEDIRepository comandaEDIRepository;

	public ModificaUltimAlbara(ComandaEDIRepository comandaEDIRepository) {
		this.comandaEDIRepository = comandaEDIRepository;
	}

	public void executar(Long codiComanda, String codiArticle, String ultimAlbara) {
		System.out.println("codiComanda: "+ codiComanda + "codiArticle: " + codiArticle + "ultimAlbara:" +ultimAlbara);
		comandaEDIRepository.updateUltimAlbara(codiComanda,codiArticle,ultimAlbara);
	}
	
}
