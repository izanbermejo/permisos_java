package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarComandaEDI {

    @Autowired ComandaEdiRepository comandaEdiRepository;

    public void executar(KeyComandaEdi keyComandaEdi){
        var comanda = comandaEdiRepository.obtenirComanda(keyComandaEdi).orElseThrow(EDIException.ComandaNoTrobada::new);
        var comandaProcessada = comanda.marcaComandaEsborrada();
        comandaEdiRepository.save(comandaProcessada);
    }
}
