package ames.comercial.inventari.internal.domain.service;

import ames.comercial.inventari.internal.domain.moviment.InformacioDevolucioImpl;
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

public class CrearMovimentDevolucio {

    public Moviment executar(CrearMovimentDevolucioRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.SORTIDA)
                .quantitat(request.quantitat())
                .devolucio(InformacioDevolucioImpl.builder()
                        .parteDevolucio(request.parteDevolucio())
                        .build())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentDevolucioRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentDevolucioRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        // Dades específiques de sortida
        String parteDevolucio();

        Optional<String> observacions();
    }

}
