package ames.comercial.clients.internal.infraestructure.adjunt;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public interface AdjuntClientRepository {

    /** Afegeix un fitxer d'una comanda o el substitueix si ja existeix */
    void add (String codiClient, InputStream stream, String nomFitxer);

    /** Elimina un fitxer d'una comanda (no retorna error en cas que no es pugui eliminar) */
    void remove (String codiClient, String nomFitxer);

    /** Retorna el fitxer d'una comanda */
    Optional<File> find (String codiClient, String nomFitxer);

}
