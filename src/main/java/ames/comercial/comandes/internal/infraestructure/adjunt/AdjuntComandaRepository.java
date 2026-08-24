package ames.comercial.comandes.internal.infraestructure.adjunt;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public interface AdjuntComandaRepository {

    /** Afegeix un fitxer d'una comanda o el substitueix si ja existeix */
    void add (long numero, InputStream stream, String nomFitxer);

    /** Elimina tots els fitxers d'una comanda (no retorna error en cas que no es puguin eliminar) */
    void removeAll (long numero);

    /** Elimina un fitxer d'una comanda (no retorna error en cas que no es pugui eliminar) */
    void remove (long numero, String nomFitxer);

    /** Retorna el fitxer d'una comanda */
    Optional<File> find (long numero, String nomFitxer);

}
