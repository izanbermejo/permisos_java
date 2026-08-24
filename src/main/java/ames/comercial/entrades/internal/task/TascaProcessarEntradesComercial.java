package ames.comercial.entrades.internal.task;

import ames.comercial.entrades.internal.application.ProcessaEntradaComercial;
import ames.comercial.entrades.internal.infraestructure.comercial.EntradaComercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.entrades", havingValue = "true", matchIfMissing = false)
public class TascaProcessarEntradesComercial {

    @Autowired EntradaComercialRepository entradaComercialRepo;
    @Autowired ProcessaEntradaComercial processaEntradaComercial;

    // Cada hora
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void executar() {
        var entradesPendents = entradaComercialRepo.pendentsProcessar();
        for (var entrada : entradesPendents) {
            processaEntradaComercial.processar(entrada);
        }
    }

}
