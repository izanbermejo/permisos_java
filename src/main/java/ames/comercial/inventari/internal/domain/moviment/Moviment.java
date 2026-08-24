package ames.comercial.inventari.internal.domain.moviment;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = MovimentImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Moviment {

    KeyArticleClient articleClient();
    String empresa();
    String magatzem();
    LocalDate data();
    TipusMoviment tipus();
    Long quantitat();
    Optional<KeyLiniaAlbara> liniaAlbara();

    // Moviment d'entrada
    Optional<InformacioEntrada> entrada();
    // Moviment de sortida (positiu és una venda)
    Optional<InformacioSortida> sortida();
    // Moviment de sortida (negatiu és una devolució)
    Optional<InformacioDevolucio> devolucio();
    // Moviment de traspas a un altre client
    Optional<InformacioTraspasClient> traspasClient();
    // Moviment de traspas a un altre magatzem
    Optional<InformacioTraspasMagatzem> traspasMagatzem();
    // Moviment de traspas a una altra empresa
    Optional<InformacioTraspasEmpresa> traspasEmpresa();

    // Observacions i camps d'auditoria
    Optional<String> observacions();
    LocalDateTime dataCreacio();
    String usuari();

    static KeyFitxa fromMoviment(Moviment moviment) {
        return KeyFitxa.of(moviment.articleClient(), moviment.empresa(), moviment.magatzem());
    }

    @Value.Check
    default void check() {
        switch (tipus()) {
            case ENTRADA -> {
                if (quantitat() > 0) {
                    Preconditions.checkArgument(entrada().isPresent(), "Moviment d'ENTRADA positiu ha de tenir informació d'entrada");
                } else {
                    Preconditions.checkArgument(entrada().isEmpty(), "Moviment d'ENTRADA negatiu no pot tenir informació d'entrada");
                }
                Preconditions.checkArgument(sortida().isEmpty(), "Moviment d'ENTRADA no pot tenir informació de sortida");
                Preconditions.checkArgument(devolucio().isEmpty(), "Moviment d'ENTRADA no pot tenir informació de devolució");
                Preconditions.checkArgument(traspasClient().isEmpty(), "Moviment d'ENTRADA no pot tenir informació de traspàs de client");
                Preconditions.checkArgument(traspasMagatzem().isEmpty(), "Moviment d'ENTRADA no pot tenir informació de traspàs de magatzem");
                Preconditions.checkArgument(traspasEmpresa().isEmpty(), "Moviment d'ENTRADA no pot tenir informació de traspàs d'empresa");
            }
            case SORTIDA -> {
                Preconditions.checkArgument(entrada().isEmpty(), "Moviment de SORTIDA no pot tenir informació d'entrada");
                if (quantitat() < 0) {
                    Preconditions.checkArgument(devolucio().isPresent(), "Moviment de SORTIDA negatiu ha de tenir informació de devolució");
                    Preconditions.checkArgument(sortida().isEmpty(), "Moviment de SORTIDA negatiu no pot tenir informació de sortida");
                } else {
                    Preconditions.checkArgument(devolucio().isEmpty(), "Moviment de SORTIDA positiu no pot tenir informació de devolució");
                    Preconditions.checkArgument(sortida().isPresent(), "Moviment de SORTIDA positiu ha de tenir informació de sortida");
                }
                Preconditions.checkArgument(traspasClient().isEmpty(), "Moviment de SORTIDA no pot tenir informació de traspàs de client");
                Preconditions.checkArgument(traspasMagatzem().isEmpty(), "Moviment de SORTIDA no pot tenir informació de traspàs de magatzem");
                Preconditions.checkArgument(traspasEmpresa().isEmpty(), "Moviment de SORTIDA no pot tenir informació de traspàs d'empresa");
            }
            case TRASPAS_CLIENT -> {
                Preconditions.checkArgument(entrada().isEmpty(), "Moviment de TRASPÀS CLIENT no pot tenir informació d'entrada");
                Preconditions.checkArgument(sortida().isEmpty(), "Moviment de TRASPÀS CLIENT no pot tenir informació de sortida");
                Preconditions.checkArgument(devolucio().isEmpty(), "Moviment TRASPÀS CLIENT no pot tenir informació de devolució");
                Preconditions.checkArgument(traspasClient().isPresent(), "Moviment de TRASPÀS CLIENT ha de tenir informació de traspàs de client");
                Preconditions.checkArgument(traspasMagatzem().isEmpty(), "Moviment de TRASPÀS CLIENT no pot tenir informació de traspàs de magatzem");
                Preconditions.checkArgument(traspasEmpresa().isEmpty(), "Moviment de TRASPÀS CLIENT no pot tenir informació de traspàs d'empresa");
            }
            case TRASPAS_MAGATZEM -> {
                Preconditions.checkArgument(entrada().isEmpty(), "Moviment de TRASPÀS MAGATZEM no pot tenir informació d'entrada");
                Preconditions.checkArgument(sortida().isEmpty(), "Moviment de TRASPÀS MAGATZEM no pot tenir informació de sortida");
                Preconditions.checkArgument(devolucio().isEmpty(), "Moviment TRASPÀS MAGATZEM no pot tenir informació de devolució");
                Preconditions.checkArgument(traspasClient().isEmpty(), "Moviment de TRASPÀS MAGATZEM no pot tenir informació de traspàs de client");
                Preconditions.checkArgument(traspasMagatzem().isPresent(), "Moviment de TRASPÀS MAGATZEM ha de tenir informació de traspàs de magatzem");
                Preconditions.checkArgument(traspasEmpresa().isEmpty(), "Moviment de TRASPÀS MAGATZEM no pot tenir informació de traspàs d'empresa");
            }
            case TRASPAS_EMPRESA -> {
                Preconditions.checkArgument(entrada().isEmpty(), "Moviment de TRASPÀS EMPRESA no pot tenir informació d'entrada");
                Preconditions.checkArgument(sortida().isEmpty(), "Moviment de TRASPÀS EMPRESA no pot tenir informació de sortida");
                Preconditions.checkArgument(devolucio().isEmpty(), "Moviment TRASPÀS EMPRESA no pot tenir informació de devolució");
                Preconditions.checkArgument(traspasClient().isEmpty(), "Moviment de TRASPÀS EMPRESA no pot tenir informació de traspàs de client");
                Preconditions.checkArgument(traspasMagatzem().isEmpty(), "Moviment de TRASPÀS EMPRESA no pot tenir informació de traspàs de magatzem");
                Preconditions.checkArgument(traspasEmpresa().isPresent(), "Moviment de TRASPÀS EMPRESA ha de tenir informació de traspàs d'empresa");
            }
            case FERRALLA, REGULARITZACIO, COMPRA_EXISTENCIES -> {
                Preconditions.checkArgument(entrada().isEmpty(), "Moviment de REGULARITZACIÓ no pot tenir informació d'entrada");
                Preconditions.checkArgument(sortida().isEmpty(), "Moviment de REGULARITZACIÓ no pot tenir informació de sortida");
                Preconditions.checkArgument(devolucio().isEmpty(), "Moviment REGULARITZACIÓ no pot tenir informació de devolució");
                Preconditions.checkArgument(traspasClient().isEmpty(), "Moviment de REGULARITZACIÓ no pot tenir informació de traspàs de client");
                Preconditions.checkArgument(traspasMagatzem().isEmpty(), "Moviment de REGULARITZACIÓ no pot tenir informació de traspàs de magatzem");
                Preconditions.checkArgument(traspasEmpresa().isEmpty(), "Moviment de REGULARITZACIÓ no pot tenir informació de traspàs d'empresa");
            }
        }
    }

}
