package ames.comercial.entrades.internal.task;

import ames.comercial.entrades.internal.application.command.NotificarErrorEntradaComercial;
import ames.comercial.entrades.internal.infraestructure.comercial.ErrorEntradaComercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.entrades", havingValue = "true", matchIfMissing = false)
public class TascaEnviarCorreuEntradesComercialError {

    @Autowired NotificarErrorEntradaComercial notificarErrorEntradaComercial;
    @Autowired ErrorEntradaComercialRepository errorEntradaComercialRepository;

    // 300.000ms (5min)
    @Scheduled(fixedDelay = 300_000, initialDelay = 300_000)
    public void executar() {
        errorEntradaComercialRepository.listarPendents().forEach(entrada -> {
            notificarErrorEntradaComercial.executar(entrada.idEntradaFabrica());
        });
    }

}
