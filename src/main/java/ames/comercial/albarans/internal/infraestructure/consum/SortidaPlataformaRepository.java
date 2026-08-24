package ames.comercial.albarans.internal.infraestructure.consum;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.consum.SortidaPlataforma;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;

import java.time.LocalDate;
import java.util.List;

public interface SortidaPlataformaRepository {

    void save(SortidaPlataforma sortida);

    /** Actualitza la data de consum registrada a la traçabilitat quan es rectifica la data del consum */
    void updateDataConsum(KeyAlbara albaraConsum, LocalDate dataConsum);

    /** Actualitza la data del traspàs registrada a la traçabilitat quan es rectifica la data del traspàs */
    void updateDataTraspas(KeyAlbara albaraTraspas, LocalDate dataTraspas);

    List<SortidaPlataforma> findByAlbaraConsum(KeyAlbara albaraConsum);

    List<SortidaPlataforma> findByLiniaConsum(KeyLiniaAlbara liniaConsum);

    void deleteByAlbaraConsum(KeyAlbara albaraConsum);

    void deleteByLiniaConsum(KeyLiniaAlbara liniaConsum);

}
