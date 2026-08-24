package ames.comercial.edi.internal.application.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(name = "comandesedi.process.schedule.enabled", havingValue = "true")
@Service
public class ImportaFitxersEntradaCron {

    @Autowired
    ImportaFitxersEntrada importaFitxersEntrada;

    @Scheduled(fixedDelayString = "${comercial.comandesedi.scheduled}")
    public void run() {
        importaFitxersEntrada.run();
    }

}