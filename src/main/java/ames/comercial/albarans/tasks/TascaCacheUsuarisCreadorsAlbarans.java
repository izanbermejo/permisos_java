package ames.comercial.albarans.tasks;

import ames.comercial.albarans.ext.IObtenirUsuarisCreadorsAlbarans;
import ames.comercial.cache.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Manté escalfada la cache dels usuaris que han creat albarans, que consulta la metadata.
 * <p>
 * La consulta triga mig segon (ha de recórrer tota la taula d'albarans) i la metadata ha de ser
 * àgil, de manera que la tasca la refresca abans que la cache expiri i així cap petició no ha de
 * calcular-la mai. S'executa en arrencar l'aplicació i després cada 5 hores, per sota de les 6 hores
 * de vida de l'entrada a {@link CacheConfig#USUARIS_CREADORS_ALBARANS}.
 */
@Component
public class TascaCacheUsuarisCreadorsAlbarans {

    private static final Logger logger = LoggerFactory.getLogger(TascaCacheUsuarisCreadorsAlbarans.class);

    @Autowired IObtenirUsuarisCreadorsAlbarans obtenirUsuarisCreadorsAlbarans;
    @Autowired CacheManager cacheManager;

    // Cada 5 hores, començant en arrencar
    @Scheduled(fixedDelay = 5 * 60 * 60 * 1000, initialDelay = 1)
    public void executar() {
        try {
            refrescar();
        } catch (Exception e) {
            logger.error("ERROR refrescant la cache dels usuaris que han creat albarans", e);
        }
    }

    /**
     * Buida l'entrada de la cache i la torna a omplir cridant el bean a través del proxy de Spring,
     * que és qui hi aplica el {@code @Cacheable}.
     */
    public synchronized void refrescar() {
        var cache = cacheManager.getCache(CacheConfig.USUARIS_CREADORS_ALBARANS);
        if (cache != null) {
            cache.clear();
        }
        obtenirUsuarisCreadorsAlbarans.all();
    }

}
