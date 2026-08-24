package ames.comercial.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    public static final String STOCK_SATELIT = "stockSatelitCache";
    public static final String USUARIS_CREADORS_ALBARANS = "usuarisCreadorsAlbaransCache";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                stockSatelit(),
                usuarisCreadorsAlbarans()
        ));
        return manager;
    }

    private CaffeineCache stockSatelit() {
        return new CaffeineCache(
                STOCK_SATELIT,
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(1)
                        .build()
        );
    }

    /**
     * Usuaris que han creat albarans en els darrers dos anys. La consulta ha de recórrer tots els
     * albarans i la llista només canvia quan algú crea el seu primer albarà, de manera que es manté
     * a la cache unes hores per no penalitzar la metadata. La tasca TascaCacheUsuarisCreadorsAlbarans
     * la refresca cada 5 hores, per sota d'aquesta vida, i així no expira mai en calent.
     */
    private CaffeineCache usuarisCreadorsAlbarans() {
        return new CaffeineCache(
                USUARIS_CREADORS_ALBARANS,
                Caffeine.newBuilder()
                        .expireAfterWrite(6, TimeUnit.HOURS)
                        .maximumSize(1)
                        .build()
        );
    }

}
