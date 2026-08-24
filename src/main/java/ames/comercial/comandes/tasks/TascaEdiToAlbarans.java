package ames.comercial.comandes.tasks;

import ames.comercial.comandes.internal.application.command.TraspassarInformacioEdiAlbarans;
import ames.comercial.comandes.internal.infraestructure.editoalbarans.EdiToAlbaransRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.editoalbarans", havingValue = "true", matchIfMissing = false)
public class TascaEdiToAlbarans {

    private static final Logger logger = LoggerFactory.getLogger(TascaEdiToAlbarans.class);

    @Autowired EdiToAlbaransRepository ediToAlbaransRepo;
    @Autowired TraspassarInformacioEdiAlbarans traspassarInformacioEdiAlbarans;

    // 60.000ms (1min) per anar actualitzant la tasca
    @Scheduled(fixedDelay = 60_000, initialDelay = 1)
    public void executar() {
        var ediToAlbaransPending = ediToAlbaransRepo.pendingEdiToAlbarans();
        for (var infoEdi : ediToAlbaransPending) {
            try {
                traspassarInformacioEdiAlbarans.executar(infoEdi);
            } catch (Exception e) {
                logger.info("ERROR executuant TascaEdiToAlbarans: {} - {}: {}", infoEdi.articleClient().artint(), infoEdi.articleClient().clicod(), infoEdi.comandaClient());
                logger.error("ERROR executuant TascaEdiToAlbarans", e);
            }
        }
    }

}
