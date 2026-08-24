package ames.comercial.cache.empleats;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class TascaCacheEmpleats {

    @Autowired ReompleCacheEmpleats reompleCacheEmpleats;

    @Scheduled(fixedDelay = 3_600_000, initialDelay = 900_000)
    public void executar() {
        reompleCacheEmpleats.executar();
    }

}
