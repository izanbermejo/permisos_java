package ames.comercial.inventari.internal.domain.service;

import ames.comercial.inventari.internal.domain.moviment.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CrearMovimentTraspasClient {

    public Moviment executar(CrearMovimentTraspasClientRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.TRASPAS_CLIENT)
                .quantitat(request.quantitat())
                .traspasClient(InformacioTraspasClientImpl.builder()
                        .clientReceptor(request.clientReceptor())
                        .isVaImplicarTraspasEmpresa(request.isVaImplicarTraspasEmpresa())
                        .build())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentTraspasClientRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentTraspasClientRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        // Dades específiques de traspàs de client
        String clientReceptor();
        boolean isVaImplicarTraspasEmpresa();

        Optional<String> observacions();
    }

}

