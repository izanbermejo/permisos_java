package ames.comercial.edi2.internal.infraestructure.embalatgeexpedicio;

import ames.comercial.edi2.internal.domain.embalatgeexpedicio.EmbalatgeExpedicio;
import ames.comercial.shared.KeyArticleClient;

import java.util.Optional;

public interface EmbalatgeExpedicioRepository {

    Optional<EmbalatgeExpedicio> find (KeyArticleClient articleClient);
    void save (KeyArticleClient articleClient, EmbalatgeExpedicio embalatgeExpedicio);

}
