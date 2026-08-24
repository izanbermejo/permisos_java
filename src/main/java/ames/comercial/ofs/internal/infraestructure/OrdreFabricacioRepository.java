package ames.comercial.ofs.internal.infraestructure;

import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.shared.KeyArticleClient;

import java.util.Optional;

public interface OrdreFabricacioRepository {

    long nextNumero();
    Optional<OrdreFabricacio> get(long numero);
    Optional<OrdreFabricacio> get(KeyArticleClient articleClient);
    void save(OrdreFabricacio of);

}