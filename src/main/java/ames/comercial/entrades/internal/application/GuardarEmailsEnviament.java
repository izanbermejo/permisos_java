package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.infraestructure.AdresesErrorEntrada;
import ames.comercial.server.service.EmailValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardarEmailsEnviament {

    @Autowired
    EmailValidation emailValidation;
    @Autowired
    AdresesErrorEntrada adresesErrorEntrada;

    @Transactional
    public void executar (String emailComercial, String emailMagatzem) {
        emailValidation.splitAndCheck(emailComercial);
        emailValidation.splitAndCheck(emailMagatzem);

        adresesErrorEntrada.guardar(emailComercial, emailMagatzem);
    }
}
