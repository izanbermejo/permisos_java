package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.embalatgeexpedicio.EmbalatgeExpedicio;
import ames.comercial.edi2.internal.infraestructure.embalatgeexpedicio.EmbalatgeExpedicioRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirEmbalatgesExpedicio {

    @Autowired EmbalatgeExpedicioRepository embalatgeExpedicioRepository;

    public Optional<EmbalatgeExpedicio> executar(KeyArticleClient keyArticleClient){
        return embalatgeExpedicioRepository.find(keyArticleClient);
    }
}
