package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.EntradesException;
import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.internal.infraestructure.comercial.EntradaComercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReprocessaEntradaComercial {

    @Autowired
    EntradaComercialRepository entradaComercialRepo;
    @Autowired
    ProcessaEntradaComercial processaEntradaComercial;

    @Transactional
    public void reprocessarEntradaComercial (String id) {
        EntradaComercial entradaComercial = entradaComercialRepo.find(id).orElseThrow(EntradesException.NoTrobaEntrada::new);
        //
        entradaComercial.checkPotReprocessar();

        processaEntradaComercial.processar(entradaComercial);
    }
}
