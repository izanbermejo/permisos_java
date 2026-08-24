package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.embalatgeexpedicio.EmbalatgeExpedicio;
import ames.comercial.edi2.internal.infraestructure.embalatgeexpedicio.EmbalatgeExpedicioRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuardarEmbalatgesExpedicio {

    @Autowired EmbalatgeExpedicioRepository embalatgeExpedicioRepository;

    public void executar(KeyArticleClient keyArticleClient, EmbalatgeExpedicio embalatgeExpedicio){
        embalatgeExpedicioRepository.save(keyArticleClient, embalatgeExpedicio);
    }
}
