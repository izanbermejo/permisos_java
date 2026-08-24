package ames.comercial.cache.articlesclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class TascaCacheArticlesClient {

    @Autowired
    ReompleCacheArticlesClient reompleCacheArticlesClient;

    @Scheduled(fixedDelay = 600_000, initialDelay = 300_000)
    public void executar() {
        reompleCacheArticlesClient.executar();
    }

}
