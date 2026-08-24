package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.internal.service.ValidarCarregarComandesMarketing;
import ames.comercial.comandes.request.CrearComandaNormalitzatRequest;
import ames.comercial.comandes.request.CrearComandaNormalitzatRequestImpl;
import ames.comercial.comandes.service.request.LiniaNormalitzatReqImpl;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Component
public class CarregarComandesMarketing {

    @Autowired ValidarCarregarComandesMarketing validarCarregarComandesMarketing;
    @Autowired CrearComandaNormalitzat crearComandaNormalitzat;

    public void executar (InputStream fitxer) {
        // Execució de la validació de la càrrega de l'stock de seguretat
        var registresCarregar = validarCarregarComandesMarketing.executar(fitxer);
        // Per cada registre llegit de l'Excel es fa una crida a la definició de l'stock de seguretat
        for (var reg : registresCarregar) {
            var request = toRequest(reg);
            crearComandaNormalitzat.executar(request);
            System.out.println(request);
        }
    }

    private CrearComandaNormalitzatRequest toRequest (RegistreCarregarComandesMarketing registre) {
        return CrearComandaNormalitzatRequestImpl.builder()
                .codiClient(registre.client)
                .comanda("MKT2026: " + registre.nomClient)
                .dataRecepcio(LocalDate.now())
                .linies(List.of(LiniaNormalitzatReqImpl.builder()
                        .linia(1L)
                        .articleClient(registre.articleClient)
                        .quantitat(registre.quantitat)
                        .dataSolicitada(LocalDate.now())
                        .dataPrevistaSortida(LocalDate.now())
                        .build()))
                .build();
    }

    public record RegistreCarregarComandesMarketing(String client, String nomClient, KeyArticleClient articleClient, long quantitat, LocalDate data) {}

}
