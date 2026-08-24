package ames.comercial.albarans.internal.infraestructure.albara;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.server.ETag;

import java.util.List;
import java.util.Optional;

public interface AlbaraRepository {

    void save(Albara albara);
    Optional<Albara> find(KeyAlbara id);
    List<Albara> findObertsClient(String codiClient);
    void delete(KeyAlbara id);
    void checkEtag(KeyAlbara id);
    Optional<ETag> etag(KeyAlbara id);

}
