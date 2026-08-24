package ames.comercial.edi2.internal.infraestructure.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;

import java.util.List;
import java.util.Optional;

public interface CapsaleraEdiRepository {

    void save(List<CapsaleraEdi> missatge);
    List<CapsaleraEdi> obtenirCapsaleraEdi(long idMissatge);
    Optional<CapsaleraEdi> obtenir (long idMissatge, long idCapsalera);

}
