package ames.comercial.inventari.internal.task;

import ames.comercial.inventari.internal.application.command.EnviarLlistatFerralla;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@ConditionalOnProperty(name = "tasca.ferralla", havingValue = "true", matchIfMissing = false)
public class TascaEnviarLlistatFerralla {

    private static final Logger logger = LoggerFactory.getLogger(TascaEnviarLlistatFerralla.class);

    @Autowired EnviarLlistatFerralla enviarLlistatFerralla;

    /**
     * Cada dia 1 de mes a les 8:15, un quart després del llistat de peces immobilitzades per no
     * encavalcar-hi els dos enviaments.
     * <p>
     * Es passa el mes en curs, no l'anterior com fa la tasca d'immobilitzades: el període de la
     * ferralla són els 12 mesos <em>anteriors</em> al mes de referència, de manera que executant-se
     * l'1 de setembre amb el mes de setembre el llistat cobreix del setembre anterior a l'agost, els
     * 12 mesos que acaben de tancar.
     */
    @Scheduled(cron = "0 15 8 1 * *")
    public void executar() {
        var mes = YearMonth.now();
        logger.info("INICI ENVIAMENT llistat de ferralla dels 12 mesos anteriors a {}", mes);
        try {
            enviarLlistatFerralla.executar(mes);
        } catch (Exception e) {
            logger.error("ERROR enviant el llistat de ferralla dels 12 mesos anteriors a " + mes, e);
        }
        logger.info("FI ENVIAMENT llistat de ferralla dels 12 mesos anteriors a {}", mes);
    }

}
