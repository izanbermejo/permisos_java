package ames.comercial.cache.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class TascaCacheClients {

    @Autowired ReompleCacheClients reompleCacheClients;

    @Scheduled(fixedDelay = 3_600_000, initialDelay = 600_000)
    public void executar() {
        reompleCacheClients.executar();
    }

}
