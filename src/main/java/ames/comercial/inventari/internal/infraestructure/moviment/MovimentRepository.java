package ames.comercial.inventari.internal.infraestructure.moviment;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.inventari.internal.domain.moviment.Moviment;

import java.util.List;
import java.util.Optional;

public interface MovimentRepository {

    List<Moviment> findByLiniaAlbara(KeyLiniaAlbara idLiniaAlbara);
    List<Moviment> findByLiniaAlbara(List<KeyLiniaAlbara> liniesAlbara);
    Optional<Moviment> findById(long id);
    void save(List<Moviment> moviment);
    void save(Moviment moviment);
    void delete(KeyLiniaAlbara idLiniaAlbara);
    void delete(List<KeyLiniaAlbara> liniesAlbara);
    void deleteById(long id);

}

