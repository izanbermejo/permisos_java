package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.internal.infraestructure.comandacomentaris.ComandaComentarisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActualitzarComentarisComanda {

    @Autowired
    ComandaComentarisRepository comentarisComandaRepo;

    public void executar(long comanda, String text) {
        comentarisComandaRepo.updateComentaris(comanda, text);
    }

}
