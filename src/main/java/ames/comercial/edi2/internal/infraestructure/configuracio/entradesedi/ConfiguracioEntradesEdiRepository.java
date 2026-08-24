package ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi;

import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;

import java.util.List;

public interface ConfiguracioEntradesEdiRepository {

    void save(ConfiguracioEntradaComanda configuracioEntradaComanda);
    List<ConfiguracioEntradaComanda> obtenirConfiguracioEdi(String ediBox, String nad02, String codiProveidor, String tipusEdi);
    List<ConfiguracioEntradaComanda> obtenirConfiguracioEdi(String codiClient);
    ConfiguracioEntradaComanda obtenirConfiguracioEdi(String codiClient, String tipusEdi);
}
