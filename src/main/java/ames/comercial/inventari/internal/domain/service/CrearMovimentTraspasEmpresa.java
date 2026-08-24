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

public class CrearMovimentTraspasEmpresa {

    public Moviment executar(CrearMovimentTraspasEmpresaRequest request) {
        return MovimentImpl.builder()
                .articleClient(request.articleClient())
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .tipus(TipusMoviment.TRASPAS_EMPRESA)
                .quantitat(request.quantitat())
                .liniaAlbara(request.liniaAlbara())
                .traspasEmpresa(InformacioTraspasEmpresaImpl.builder()
                        .empresaReceptora(request.empresaReceptora())
                        .isVaImplicarTraspasMagatzem(request.isVaImplicarTraspasMagatzem())
                        .build())
                .observacions(request.observacions())
                .dataCreacio(LocalDateTime.now())
                .usuari(RequestThread.codiUsuari())
                .build();
    }

    @JsonDeserialize(builder = CrearMovimentTraspasEmpresaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearMovimentTraspasEmpresaRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();

        // Dades específiques de traspàs d'empresa
        String empresaReceptora();
        Optional<KeyLiniaAlbara> liniaAlbara();
        boolean isVaImplicarTraspasMagatzem();

        Optional<String> observacions();
    }

}

