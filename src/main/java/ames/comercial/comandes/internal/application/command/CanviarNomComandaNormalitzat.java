package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.domain.comanda.InformacioClientImpl;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanviarNomComandaNormalitzat {

    @Autowired ComandaRepository comandaRepo;

	@Transactional
	public void executar (long idComanda, String nomNou) {
        var comanda = comandaRepo.find(idComanda).orElseThrow(ComandaNoExisteix::new);
        var infoClient = InformacioClientImpl.builder()
                .from(comanda.informacioClient())
                .identificador(nomNou)
                .build();
        comanda.canviarInformacioClient(infoClient);
        comandaRepo.save(comanda);
	}

}
