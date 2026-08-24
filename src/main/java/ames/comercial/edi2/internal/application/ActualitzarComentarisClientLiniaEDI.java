package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.linia.LiniaEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActualitzarComentarisClientLiniaEDI {

    @Autowired LiniaEdiRepository comentarisRepo;

    public void executar(KeyComandaEdi clauLiniaComanda, long idLinia, String text) {
        if (idLinia != 0){
            comentarisRepo.updateComentarisClient(clauLiniaComanda, idLinia, text);
        } else {
            comentarisRepo.updateComentarisClient(clauLiniaComanda, text);
        }
    }

}
