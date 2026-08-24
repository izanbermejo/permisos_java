package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.infraestructure.AdresesErrorEntrada;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenirEmailsEnviament {
    @Autowired AdresesErrorEntrada adresesErrorEntrada;

    public List<JSONObject> executar() {
        return adresesErrorEntrada.obtenir();
    }
}
