package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.ConfiguracioEdi;
import ames.comercial.edi2.internal.domain.ConfiguracioEdiImpl;
import ames.comercial.edi2.internal.infraestructure.configuracio.aviexp.AviExpRepository;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirConfiguracioEdi {

    @Autowired AviExpRepository aviExpRepository;
    @Autowired ConfiguracioEntradesEdiRepository configEntradesEdiRepository;

    public ConfiguracioEdi get(String codiClient){

        var aviexpConfig = aviExpRepository.obtenirConfiguracio(codiClient);
        var entradesEdiConfig = configEntradesEdiRepository.obtenirConfiguracioEdi(codiClient);

        return ConfiguracioEdiImpl.builder()
                .configuracioEntradaComandes(entradesEdiConfig)
                .configuracioAviExp(aviexpConfig)
                .build();
    }
}
