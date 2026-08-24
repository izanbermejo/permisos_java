package ames.comercial.edi2.internal.application.query;

import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEntradesEdiSql;
import ames.comercial.shared.SharedExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuardarConfiguracioEDI2 {

    @Autowired
    ConfiguracioEntradesEntradesEdiSql configuracioEntradesEdiSql;
    @Autowired
    ObtenirClientAds obtenirClientAds;

    public void guardar(ConfiguracioEntradaComanda req) {

        var codiClient = req.codiClient();

        obtenirClientAds.get(codiClient).orElseThrow(() -> new SharedExceptions.ClientNoExisteix(codiClient));

        configuracioEntradesEdiSql.save(req);
    }

}
