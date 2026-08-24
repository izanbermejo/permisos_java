package ames.comercial.albarans.internal.infraestructure.linia;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.server.ETag;

import java.util.List;
import java.util.Optional;

public interface LiniaAlbaraRepository {

    long nextNumero(KeyAlbara idAlbara);

    void save(LiniaAlbara linia);
    void save(List<LiniaAlbara> linies);

    Optional<LiniaAlbara> find(KeyLiniaAlbara id);
    Optional<LiniaAlbara> find(KeyAlbara idAlbara, long linia);
    List<LiniaAlbara> findByAlbara(KeyAlbara idAlbara);

    void delete(KeyLiniaAlbara id);
    void deleteByAlbara(KeyAlbara idAlbara);

    boolean isHiHaLiniesFacturades(KeyAlbara idAlbara);

    void checkEtag(KeyLiniaAlbara id);
    Optional<ETag> etag(KeyLiniaAlbara id);

}
