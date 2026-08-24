package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;
import ames.comercial.edi2.internal.infraestructure.capsalera.CapsaleraEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirCapsaleraEdi {

    @Autowired CapsaleraEdiRepository capsaleraEdiRepository;

    public List<CapsaleraEdi> executar(long idMissatge) {
        return capsaleraEdiRepository.obtenirCapsaleraEdi(idMissatge);
    }
}
