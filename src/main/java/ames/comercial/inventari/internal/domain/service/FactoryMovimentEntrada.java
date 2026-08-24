package ames.comercial.inventari.internal.domain.service;

import ames.comercial.inventari.internal.domain.moviment.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class FactoryMovimentEntrada {

    public Moviment executar(CrearMovimentEntradaRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.ENTRADA)
                .quantitat(request.quantitat())
                .entrada(InformacioEntradaImpl.builder()
                        .of(request.of())
                        .idEntrada(request.idEntrada())
                        .idEntradaFabrica(request.idEntradaFabrica())
                        .build())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentEntradaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentEntradaRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        // Dades específiques d'entrada
        Long of();
        String idEntrada();
        String idEntradaFabrica();

        Optional<String> observacions();
    }

}
