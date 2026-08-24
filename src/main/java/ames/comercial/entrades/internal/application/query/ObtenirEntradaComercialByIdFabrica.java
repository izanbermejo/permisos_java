package ames.comercial.entrades.internal.application.query;

import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.internal.infraestructure.comercial.EntradaComercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirEntradaComercialByIdFabrica {

    @Autowired EntradaComercialRepository entradaComercialRepo;

    public List<EntradaComercial> executar(String idEntradaFabrica) {
        return entradaComercialRepo.obtenirByIdFabrica(idEntradaFabrica);
    }
}