package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.internal.service.ValidarCarregarStockSeguretat;
import ames.comercial.comandes.request.DefinirStockSeguretatRequest;
import ames.comercial.comandes.request.DefinirStockSeguretatRequestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;

@Component
public class CarregarStocksSeguretat {

    @Autowired ValidarCarregarStockSeguretat validarCarregarStockSeguretat;
    @Autowired DefinirStockSeguretat definirStockSeguretat;

    @Transactional
    public void executar (InputStream fitxer) {
        // Execució de la validació de la càrrega de l'stock de seguretat
        var registresCarregar = validarCarregarStockSeguretat.executar(fitxer);
        // Per cada registre llegit de l'Excel es fa una crida a la definició de l'stock de seguretat
        for (var reg : registresCarregar) {
            definirStockSeguretat.executar(reg.articleclient, reg.toRequest());
        }
    }

    public record RegistreCarregarStockSeguretat(String articleclient, long quantitat, LocalDate data, boolean isStockClient) {
        public DefinirStockSeguretatRequest toRequest() {
            return DefinirStockSeguretatRequestImpl.builder()
                    .quantitat(quantitat)
                    .dataSolicitada(data)
                    .stockSeguretatClient(isStockClient)
                    .build();
        }
    }

}
