package ames.comercial.edi.internal.application.command;

import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EsborraComanda {

    @Autowired
    ComandaEDIRepository comandaEDIRepository;

    public EsborraComanda(ComandaEDIRepository comandaEDIRepository) {
        this.comandaEDIRepository = comandaEDIRepository;
    }

    public void executar(Long codiComanda) {
        comandaEDIRepository.delete(codiComanda);
    }

}
