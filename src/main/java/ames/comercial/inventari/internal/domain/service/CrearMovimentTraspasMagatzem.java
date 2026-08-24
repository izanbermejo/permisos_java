package ames.comercial.inventari.internal.domain.service;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.inventari.internal.domain.moviment.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CrearMovimentTraspasMagatzem {

    public Moviment executar(CrearMovimentTraspasMagatzemRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.TRASPAS_MAGATZEM)
                .quantitat(request.quantitat())
                .liniaAlbara(request.liniaAlbara())
                .traspasMagatzem(InformacioTraspasMagatzemImpl.builder()
                        .magatzemReceptor(request.magatzemReceptor())
                        .isVaImplicarTraspasEmpresa(request.isVaImplicarTraspasEmpresa())
                        .build())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentTraspasMagatzemRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentTraspasMagatzemRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        // Dades específiques de traspàs de magatzem
        String magatzemReceptor();
        Optional<KeyLiniaAlbara> liniaAlbara();
        boolean isVaImplicarTraspasEmpresa();

        Optional<String> observacions();
    }

}

