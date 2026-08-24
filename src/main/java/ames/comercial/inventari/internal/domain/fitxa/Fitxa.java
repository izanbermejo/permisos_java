package ames.comercial.inventari.internal.domain.fitxa;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;

@JsonDeserialize(builder = FitxaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Fitxa {

    KeyFitxa id();
    Long stock();
    Long stockReservat();
    boolean isActiu();
    LocalDate dataCreacio();

    @Derived
    default KeyArticleClient articleClient() { return id().articleClient(); }

    @Derived
    default String magatzem() { return id().magatzem(); }

    @Derived
    default Empresa empresa() { return Empresa.getByClau(id().empresa()); }

    static Fitxa create(KeyFitxa id, long quantitatInicial) {
        return FitxaImpl.builder()
                .id(id)
                .stock(quantitatInicial)
                .stockReservat(0L)
                .isActiu(true)
                .dataCreacio(LocalDate.now())
                .build();
    }

    default Fitxa incrementaReserva(long quantitat) {
        return FitxaImpl.builder()
                .from(this)
                .stockReservat(stockReservat() + quantitat)
                .build();
    }

     default Fitxa actualitzaReserva(long quantitat) {
        return FitxaImpl.builder()
                .from(this)
                .stockReservat(quantitat)
                .build();
    }

}
