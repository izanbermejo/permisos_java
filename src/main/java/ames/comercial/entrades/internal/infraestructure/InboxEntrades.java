package ames.comercial.entrades.internal.infraestructure;

import java.util.Map;

public interface InboxEntrades {

    boolean exists (String id);

    void save (String id, String contingut, OrigenEntrada origenEntrada);
    void marcaProcessat (String id);
    void marcaError (String id, String error);
    Map<String, String> obtenirPendents(OrigenEntrada origenEntrada);

    enum OrigenEntrada {
        JSON, TXT;
    }

}
