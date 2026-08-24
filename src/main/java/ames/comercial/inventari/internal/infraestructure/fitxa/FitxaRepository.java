package ames.comercial.inventari.internal.infraestructure.fitxa;

import ames.comercial.inventari.internal.domain.fitxa.Fitxa;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxa;
import ames.comercial.shared.KeyArticleClient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FitxaRepository {

    void save(Fitxa fitxa);
    List<Fitxa> find(KeyArticleClient articleClient);
    List<Fitxa> find(Set<KeyArticleClient> articleClient, String empresa, String magatzem);
    Optional<Fitxa> find(KeyFitxa id);
    void updateStock(KeyFitxa id, long incrementStock, long incrementStockReservat);
    long updateStockReservat(KeyFitxa id, long stockReservat);
    long incrementaStockReservat(KeyFitxa id, long incrementStockReservat);

}

