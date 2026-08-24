package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.liniacomandacomentaris.LiniaComandaComentarisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActualitzarComentarisInternsLiniaComanda {

    @Autowired LiniaComandaComentarisRepository comentarisRepo;

    public void executar(KeyLiniaComanda clauLiniaComanda, String text) {
        comentarisRepo.updateComentarisInterns(clauLiniaComanda, text);
    }

}
