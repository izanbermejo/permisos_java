package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirConfiguracioEntradaEdi {

    @Autowired
    ConfiguracioEntradesEdiRepository comandaEdiRepository;

    public List<ConfiguracioEntradaComanda> executar(String ediBox, String nad02, String codiProveidor, String tipus_missatge) {
        return comandaEdiRepository.obtenirConfiguracioEdi(ediBox,nad02, codiProveidor, tipus_missatge);
    }
}