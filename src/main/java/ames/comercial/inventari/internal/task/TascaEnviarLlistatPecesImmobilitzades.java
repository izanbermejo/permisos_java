package ames.comercial.inventari.internal.task;

import ames.comercial.inventari.internal.application.command.EnviarLlistatPecesImmobilitzades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@ConditionalOnProperty(name = "tasca.immobilitzades", havingValue = "true", matchIfMissing = false)
public class TascaEnviarLlistatPecesImmobilitzades {

    private static final Logger logger = LoggerFactory.getLogger(TascaEnviarLlistatPecesImmobilitzades.class);

    @Autowired EnviarLlistatPecesImmobilitzades enviarLlistatPecesImmobilitzades;

    /**
     * Cada dia 1 de mes a les 8:00. El llistat és del mes anterior, que és el darrer mes tancat:
     * el mes en curs tot just comença i encara no té cap moviment.
     */
    @Scheduled(cron = "0 0 8 1 * *")
    public void executar() {
        var mes = YearMonth.now().minusMonths(1);
        logger.info("INICI ENVIAMENT llistat de peces immobilitzades de {}", mes);
        try {
            enviarLlistatPecesImmobilitzades.executar(mes);
        } catch (Exception e) {
            logger.error("ERROR enviant el llistat de peces immobilitzades de " + mes, e);
        }
        logger.info("FI ENVIAMENT llistat de peces immobilitzades de {}", mes);
    }

}
