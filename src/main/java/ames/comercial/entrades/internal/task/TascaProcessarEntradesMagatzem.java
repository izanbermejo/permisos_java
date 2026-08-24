package ames.comercial.entrades.internal.task;

import ames.comercial.entrades.internal.application.ProcessaEntradaMagatzem;
import ames.comercial.entrades.internal.infraestructure.magatzem.EntradaMagatzemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.entrades", havingValue = "true", matchIfMissing = false)
public class TascaProcessarEntradesMagatzem {

    @Autowired EntradaMagatzemRepository entradaMagatzemRepo;
    @Autowired ProcessaEntradaMagatzem processaEntradaMagatzem;

    // Cada hora
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void executar() {
        var entradesPendents = entradaMagatzemRepo.pendentsProcessar();
        for (var entrada : entradesPendents) {
            processaEntradaMagatzem.processar(entrada);
        }
    }

}
