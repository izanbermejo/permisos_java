package ames.comercial.edi2.internal.infraestructure.comanda;

import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi.Estat;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;

import java.util.List;
import java.util.Optional;

public interface ComandaEdiRepository {

    void save(List<ComandaEdi> comandes);
    void save(ComandaEdi comandes);
    Optional<ComandaEdi> obtenirComanda (KeyComandaEdi keyComandaEdi);
    List<ComandaEdi> obtenirComandabyNumComanda (String numComanda);
    void marcaEstatComanda(long idMissatge, long idComanda, Estat event, Optional<String> missatge);
    List<KeyComandaEdi> obtenirComandesPerLligar();
    List<ComandaEdi> obtenirComandesPerMissatge(long idMissatge);
}
