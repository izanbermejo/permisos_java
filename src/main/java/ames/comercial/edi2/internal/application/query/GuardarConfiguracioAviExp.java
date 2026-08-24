package ames.comercial.edi2.internal.application.query;

import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.edi2.internal.domain.ConfiguracioAviExp;
import ames.comercial.edi2.internal.infraestructure.configuracio.aviexp.AviExpRepository;
import ames.comercial.shared.SharedExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuardarConfiguracioAviExp {

    @Autowired AviExpRepository aviExpRepository;
    @Autowired ObtenirClientAds obtenirClientAds;

    public void guardar(ConfiguracioAviExp req) {

        var codiClient = req.codiClient();

        obtenirClientAds.get(codiClient).orElseThrow(() -> new SharedExceptions.ClientNoExisteix(codiClient));

        aviExpRepository.save(req);
    }

}

