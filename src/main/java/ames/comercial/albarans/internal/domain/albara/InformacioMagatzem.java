package ames.comercial.albarans.internal.domain.albara;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = InformacioMagatzemImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioMagatzem {

    Optional<String> numAlbaraMix();
    Optional<Long> pesBrut();
    Optional<Long> bultos();
    boolean isEnServei();
    boolean isEnPreparacio();
    boolean isServit();
    boolean isEntregat();
    Optional<LocalDateTime> dataPrevista();
    Optional<LocalDateTime> dataEnviament();
    Optional<InformacioPalet> paletsTipus1();
    Optional<InformacioPalet> paletsTipus2();
    Optional<InformacioPalet> paletsTipus3();
    Optional<String> matricula();
    Optional<String> numeroAvis();
    boolean isAvisEnviat();

    /**
     * Indica si l'albarà té algun moviment de magatzem actiu (en preparació, en servei, servit o
     * entregat). Mentre en tingui, no es pot reobrir ni modificar les seves línies.
     */
    default boolean teMovimentMagatzem() {
        return isEnPreparacio() || isEnServei() || isServit() || isEntregat();
    }

    static InformacioMagatzem empty() {
        return InformacioMagatzemImpl.builder()
                .isEnServei(false)
                .isEnPreparacio(false)
                .isServit(false)
                .isEntregat(false)
                .isAvisEnviat(false)
                .build();
    }

    default InformacioMagatzem entregar() {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isEntregat(true)
                .isEnServei(true)
                .isEnPreparacio(true)
                .isServit(true)
                .build();
    }

    default InformacioMagatzem servir() {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isServit(true)
                .isEnServei(true)
                .isEnPreparacio(true)
                .build();
    }

    default InformacioMagatzem desferServir() {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isServit(false)
                .isEnServei(false)
                .isEnPreparacio(false)
                .pesBrut(Optional.empty())
                .bultos(Optional.empty())
                .paletsTipus1(Optional.empty())
                .paletsTipus2(Optional.empty())
                .paletsTipus3(Optional.empty())
                .build();
    }

    default InformacioMagatzem enServei() {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isEnServei(true)
                .build();
    }

    default InformacioMagatzem enPreparacio() {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isEnPreparacio(true)
                .build();
    }

    default InformacioMagatzem desferEnPreparacio() {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isEnPreparacio(false)
                .build();
    }

    default InformacioMagatzem actualitzarEstats(boolean isEnPreparacio, boolean isEnServei, boolean isServit, boolean isReiniciarPesBultos) {
        return InformacioMagatzemImpl.builder()
                .from(this)
                .isEnPreparacio(isEnPreparacio)
                .isEnServei(isEnServei)
                .isServit(isServit)
                .pesBrut(isReiniciarPesBultos ? Optional.empty() : this.pesBrut())
                .bultos(isReiniciarPesBultos ? Optional.empty() : this.bultos())
                .paletsTipus1(isReiniciarPesBultos ? Optional.empty() : this.paletsTipus1())
                .paletsTipus2(isReiniciarPesBultos ? Optional.empty() : this.paletsTipus2())
                .paletsTipus3(isReiniciarPesBultos ? Optional.empty() : this.paletsTipus3())
                .build();
    }

    @JsonDeserialize(builder = InformacioPaletImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface InformacioPalet {
        long numero();
        String alsada();
    }

}
