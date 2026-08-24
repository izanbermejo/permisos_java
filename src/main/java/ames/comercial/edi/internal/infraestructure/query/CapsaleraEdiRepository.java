package ames.comercial.edi.internal.infraestructure.query;

import ames.comercial.edi.internal.domain.programaentrega.CapsaleraEdi;

import java.util.List;

public interface CapsaleraEdiRepository {
    List<CapsaleraEdi> obtenirCapsaleraEdi(String pathEdi);
}
