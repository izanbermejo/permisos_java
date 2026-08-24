package ames.comercial.albarans.internal.infraestructure.facturacio;

import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbara;

import java.util.List;

public interface FacturacioLiniaAlbaraRepository {

    void save(FacturacioLiniaAlbara liniaAlbara);
    List<FacturacioLiniaAlbara> findByFactura(String empresa, String codiFactura);
    void deleteByFactura(String empresa, String codiFactura);

}
