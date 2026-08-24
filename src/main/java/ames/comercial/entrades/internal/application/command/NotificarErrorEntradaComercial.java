package ames.comercial.entrades.internal.application.command;

import ames.comercial.entrades.internal.infraestructure.comercial.ErrorEntradaComercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificarErrorEntradaComercial {
    @Autowired EnviarNotificacioEntradaComercialError enviarNotificacioEntradaComercialError;
    @Autowired ErrorEntradaComercialRepository errorEntradaComercialRepository;

    @Transactional
    public void executar(String idEntrada) {
        errorEntradaComercialRepository.marcarEnviat(idEntrada);
        enviarNotificacioEntradaComercialError.executar(idEntrada);
    }
}
