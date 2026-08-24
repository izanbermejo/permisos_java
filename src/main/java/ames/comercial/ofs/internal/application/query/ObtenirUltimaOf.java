package ames.comercial.ofs.internal.application.query;

import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.ofs.response.ObtenirOFResponse;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirUltimaOf {

    @Autowired OrdreFabricacioRepository ofRepo;
    @Autowired ObtenirOF obtenirOF;

    public Optional<ObtenirOFResponse> get(KeyArticleClient articleClient) {
        var ultimaOf = ofRepo.get(articleClient);
        if (ultimaOf.isEmpty())
            return Optional.empty();
        return obtenirOF.get(ultimaOf.get().numero());
    }

}
