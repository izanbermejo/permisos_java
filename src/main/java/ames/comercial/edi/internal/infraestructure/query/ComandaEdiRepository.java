package ames.comercial.edi.internal.infraestructure.query;

import ames.comercial.edi.internal.domain.programaentrega.ComandaEdi;

import java.util.List;

public interface ComandaEdiRepository {
    List<ComandaEdi> obtenirComandesPerMissatge(String pathEdi);
}
