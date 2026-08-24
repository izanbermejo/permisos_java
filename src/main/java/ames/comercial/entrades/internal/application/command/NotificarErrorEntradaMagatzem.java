package ames.comercial.entrades.internal.application.command;

import ames.comercial.entrades.internal.infraestructure.magatzem.ErrorEntradaMagatzemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificarErrorEntradaMagatzem {
    @Autowired EnviarNotificacioEntradaMagatzemError enviarNotificacioEntradaMagatzemError;
    @Autowired ErrorEntradaMagatzemRepository errorEntradaMagatzemRepository;

    @Transactional
    public void executar(String idEntrada, String magatzem) {
        errorEntradaMagatzemRepository.marcarEnviat(idEntrada);
        enviarNotificacioEntradaMagatzemError.executar(idEntrada, magatzem);
    }
}
