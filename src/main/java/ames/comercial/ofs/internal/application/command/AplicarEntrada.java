package ames.comercial.ofs.internal.application.command;

import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AplicarEntrada {

    @Autowired OrdreFabricacioRepository ofRepo;

    @Transactional
    public void executar(KeyArticleClient articleClient, long quantitat) {
        // Obtenció de l'última OF per aquest articleclient
        var optOf = ofRepo.get(articleClient);
        optOf.ifPresent(of -> {
            ofRepo.save(of.aplicaEntrada(quantitat));
        });
    }
}
