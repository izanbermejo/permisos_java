package ames.comercial.inventari.internal.domain.service;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.inventari.internal.domain.moviment.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class FactoryMovimentSortida {

    public Moviment executar(CrearMovimentSortidaRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.SORTIDA)
                .quantitat(request.quantitat())
                .liniaAlbara(request.liniaAlbara())
                .sortida(InformacioSortidaImpl.builder()
                        .client(request.client())
                        .liniaComanda(request.liniaComanda())
                        .build())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentSortidaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentSortidaRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        // Dades específiques de sortida
        String client();
        // Tota sortida prové d'una línia de comanda (també els consums, que serveixen la comanda pendent FIFO)
        KeyLiniaComanda liniaComanda();
        KeyLiniaAlbara liniaAlbara();

        Optional<String> observacions();
    }

}
