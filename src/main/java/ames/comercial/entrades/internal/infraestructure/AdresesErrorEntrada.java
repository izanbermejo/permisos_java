package ames.comercial.entrades.internal.infraestructure;

import org.json.JSONObject;

import java.util.List;

public interface AdresesErrorEntrada {

    List<JSONObject> obtenir();
    String obtenirComercial();
    String obtenirMagatzem();
    void guardar(String emailComercial, String emailMagatzem);
}
