package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.EntradesException;
import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.internal.infraestructure.comercial.EntradaComercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanviOfEntradaComercial {

    @Autowired
    EntradaComercialRepository entradaComercialRepo;
    @Autowired
    ProcessaEntradaComercial processaEntradaComercial;

    @Transactional
    public String canviOfEntradaComercial(String id, long of) {
        EntradaComercial entradaComercial = entradaComercialRepo.find(id).orElseThrow(() -> new EntradesException.NoTrobaEntrada());

        EntradaComercial entradaComercialModificada = entradaComercial.canviOf(of);

        return processaEntradaComercial.processar(entradaComercialModificada);
    }


}
