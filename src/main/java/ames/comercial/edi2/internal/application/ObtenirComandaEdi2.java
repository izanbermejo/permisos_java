package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirComandaEdi2 {

    @Autowired ComandaEdiRepository comandaEdiRepository;

    public Optional<ComandaEdi> executar(KeyComandaEdi keyLiniaComanda) {
        return comandaEdiRepository.obtenirComanda(keyLiniaComanda);
    }
}
