package ames.comercial.albarans.tasks;

import ames.comercial.albarans.internal.application.command.EnviarAvisAlbaransNoRecollitsComercial;
import ames.comercial.albarans.internal.application.command.EnviarAvisAlbaransNoRecollitsMagatzem;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransServitsNoRecollits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cada dia a les 17:00 avisa dels albarans que el magatzem ja ha servit però que el transportista encara
 * no ha recollit: un correu per a cada persona que els ha creat i un altre per a cada magatzem.
 * <p>
 * La consulta es fa un sol cop i es passa als dos avisos: ha de recórrer tota la taula d'albarans (el
 * filtre és sobre el JSON de la informació de magatzem) i no val la pena repetir-la. Cada avís es
 * protegeix a part perquè si un falla l'altre s'enviï igualment.
 */
@Component
@ConditionalOnProperty(name = "tasca.norecollits", havingValue = "true", matchIfMissing = false)
public class TascaEnviarAvisAlbaransNoRecollits {

    private static final Logger logger = LoggerFactory.getLogger(TascaEnviarAvisAlbaransNoRecollits.class);

    @Autowired ObtenirAlbaransServitsNoRecollits obtenirAlbaransServitsNoRecollits;
    @Autowired EnviarAvisAlbaransNoRecollitsComercial enviarAvisComercial;
    @Autowired EnviarAvisAlbaransNoRecollitsMagatzem enviarAvisMagatzem;

    @Scheduled(cron = "0 0 17 * * *")
    public void executar() {
        logger.info("INICI ENVIAMENT dels avisos d'albarans servits i no recollits");
        try {
            var albarans = obtenirAlbaransServitsNoRecollits.executar();
            logger.info("{} albarans servits i no recollits", albarans.size());

            try {
                enviarAvisComercial.executar(albarans);
            } catch (Exception e) {
                logger.error("ERROR enviant els avisos d'albarans servits i no recollits a les persones", e);
            }
            try {
                enviarAvisMagatzem.executar(albarans);
            } catch (Exception e) {
                logger.error("ERROR enviant els avisos d'albarans servits i no recollits als magatzems", e);
            }
        } catch (Exception e) {
            logger.error("ERROR obtenint els albarans servits i no recollits", e);
        }
        logger.info("FI ENVIAMENT dels avisos d'albarans servits i no recollits");
    }

}
