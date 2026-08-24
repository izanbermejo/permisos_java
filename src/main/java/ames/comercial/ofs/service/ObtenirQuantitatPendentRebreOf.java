package ames.comercial.ofs.service;

import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class ObtenirQuantitatPendentRebreOf {

    @Autowired OrdreFabricacioRepository ofRepository;

    public long executar(KeyArticleClient articleClient) {
        return ofRepository.get(articleClient)
                .filter(Predicate.not(OrdreFabricacio::isAnulada))  // La OF no ha d'estar marcada com anul·lada
                .map(OrdreFabricacio::quantitatPendent)
                .orElse(0L);
    }

}
