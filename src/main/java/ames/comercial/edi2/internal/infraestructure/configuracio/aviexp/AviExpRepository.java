package ames.comercial.edi2.internal.infraestructure.configuracio.aviexp;

import ames.comercial.edi2.internal.domain.ConfiguracioAviExp;

public interface AviExpRepository {

    void save(ConfiguracioAviExp configuracioAviExp);
    ConfiguracioAviExp obtenirConfiguracio(String codiClient);

}
