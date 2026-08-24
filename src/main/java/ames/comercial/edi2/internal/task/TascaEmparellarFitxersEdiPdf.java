package ames.comercial.edi2.internal.task;

import ames.comercial.edi2.internal.application.LligarEdiPdf;
import ames.comercial.edi2.internal.infraestructure.inbox.InboxEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "tasca.edis", havingValue = "true", matchIfMissing = false)
public class TascaEmparellarFitxersEdiPdf {

    @Autowired LligarEdiPdf processarFitxersEdiPdf;
    @Autowired InboxEdiRepository inboxEdiRepository;

    // 60.000ms (1min)
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void executar() throws IOException {
        var obtenirMissatges = inboxEdiRepository.obtenirPendentsPerLligar();

        for (var reg : obtenirMissatges.entrySet()) {
            try {
                long idMissatge = reg.getKey();
                String txtMissatge = reg.getValue();
                processarFitxersEdiPdf.executar(txtMissatge, idMissatge);
            } catch (Exception e) {
                System.out.println("PDF no trobat id del Missatge: " + reg.getKey());
            }
        }
    }
}