package ames.comercial.cache.stocksatelit;

import ames.comercial.cache.CacheConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StockSatelitCacheService {

    private final StockSatelitService stockService;
    private final CacheManager cacheManager;
    private volatile LocalDateTime ultimRefresh;

    public StockSatelitCacheService(StockSatelitService stockService,
                                    CacheManager cacheManager) {
        this.stockService = stockService;
        this.cacheManager = cacheManager;
    }

    // Cada 30 minuts
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void refreshCache() {
        refresh();
    }

    public synchronized void refresh() {
        Cache cache = cacheManager.getCache(CacheConfig.STOCK_SATELIT);
        if (cache != null)
            cache.clear();
        stockService.obtenirRegistresStockSatelit();
        ultimRefresh = LocalDateTime.now();
    }

    public Optional<LocalDateTime> obtenirUltimRefresh() {
        return Optional.ofNullable(ultimRefresh);
    }

}
