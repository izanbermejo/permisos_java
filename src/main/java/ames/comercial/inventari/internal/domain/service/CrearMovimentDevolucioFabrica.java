package ames.comercial.inventari.internal.domain.service;

import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.domain.moviment.MovimentImpl;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CrearMovimentDevolucioFabrica {

    public Moviment executar(CrearMovimentDevolucioFabricaRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.ENTRADA)
                .quantitat(request.quantitat())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentDevolucioFabricaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentDevolucioFabricaRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        Optional<String> observacions();
    }

}
