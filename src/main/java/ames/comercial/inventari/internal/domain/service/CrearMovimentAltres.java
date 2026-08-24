package ames.comercial.inventari.internal.domain.service;

import ames.comercial.inventari.internal.domain.moviment.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CrearMovimentAltres {

    public Moviment executar(CrearMovimentAltresRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(request.tipus())
                .quantitat(request.quantitat())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentAltresRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentAltresRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();
        TipusMoviment tipus();
        Optional<String> observacions();

        @Value.Check
        default void check() {
            if (tipus() != TipusMoviment.FERRALLA &&
                tipus() != TipusMoviment.REGULARITZACIO &&
                tipus() != TipusMoviment.COMPRA_EXISTENCIES) {
                throw new IllegalArgumentException(
                    "CrearMovimentAltres només permet els tipus: FERRALLA, REGULARITZACIO o COMPRA_EXISTENCIES. Tipus rebut: " + tipus());
            }
        }
    }

}

