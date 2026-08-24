package ames.comercial.entrades.internal.task;

import ames.comercial.entrades.internal.application.command.NotificarErrorEntradaMagatzem;
import ames.comercial.entrades.internal.infraestructure.magatzem.ErrorEntradaMagatzemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.entrades", havingValue = "true", matchIfMissing = false)
public class TascaEnviarCorreuEntradesMagatzemError {

    @Autowired NotificarErrorEntradaMagatzem notificarErrorEntradaMagatzem;
    @Autowired ErrorEntradaMagatzemRepository errorEntradaMagatzemRepository;

    // 300.000ms (5min)
    @Scheduled(fixedDelay = 300_000, initialDelay = 300_000)
    public void executar() {
        errorEntradaMagatzemRepository.listarPendents().forEach(entrada -> {
            notificarErrorEntradaMagatzem.executar(entrada.idEntradaFabrica(), entrada.magatzem());
        });
    }

}
