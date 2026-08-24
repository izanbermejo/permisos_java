package ames.comercial.comandes.tasks;

import ames.comercial.advantage.internal.ObtenirActualitzacionsPreus;
import ames.comercial.comandes.internal.application.command.ActualitzacioPreuArticle;
import ames.comercial.shared.KeyArticleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.actualitzaciopreus", havingValue = "true", matchIfMissing = false)
public class TascaActualitzacioPreus {

    private static final Logger logger = LoggerFactory.getLogger(TascaActualitzacioPreus.class);

    @Autowired ActualitzacioPreuArticle actualitzarPreu;

    // 300.000ms (5min) per anar actualitzant la tasca
    @Scheduled(fixedDelay = 300_000, initialDelay = 300_000)
    public void executar() {
        logger.info("INICI TASCA ACTUALITZACIÓ PREUS");
        var actualitzacionsPreu = new ObtenirActualitzacionsPreus().get();
        for (var a : actualitzacionsPreu) {
            actualitzarPreu.executar(KeyArticleClient.of(a.artint(), a.clicod()), a.preu(), a.divisa());
        }
        logger.info("FI TASCA ACTUALITZACIÓ PREUS");
    }

}
