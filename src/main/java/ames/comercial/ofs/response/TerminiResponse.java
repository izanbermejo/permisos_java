package ames.comercial.ofs.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@JsonDeserialize(builder = TerminiResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface TerminiResponse {

    LocalDate data();
    LocalDate dataSortida();
    long quantitat();
    long quantitatAnterior();
    long quantitatRebuda();
    boolean isStockSeguretat();
    long acumulatActual();
    long acumulatAnterior();
    @Value.Derived
    default long quantitatPendent() { return Math.max(0, quantitat() - quantitatRebuda()); }
    @Value.Derived
    default boolean isPendent() {
        return quantitatPendent() > 0;
    }
    @Value.Derived
    default boolean hasQuantitat() {
        return quantitat() > 0;
    }
    @Value.Derived
    default int setmana() {
        return data().get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    static TerminiResponse nou(long quantitat, LocalDate data) {
        return nou(quantitat, false, data, data);
    }

    static TerminiResponse nou(long quantitat, boolean isStockSeguretat, LocalDate dataClient, LocalDate dataSortida) {
        return TerminiResponseImpl.builder()
                .data(dataClient)
                .dataSortida(dataSortida)
                .quantitat(quantitat)
                .quantitatAnterior(0)
                .quantitatRebuda(0)
                .isStockSeguretat(isStockSeguretat)
                .build();
    }

    default TerminiResponse nouAnterior() {
        return TerminiResponseImpl.builder()
                .from(this)
                .quantitatAnterior(this.quantitat())
                .quantitat(this.quantitatPendent())
                .quantitatRebuda(0L)
                .build();
    }

    default TerminiResponse nouAnterior(long novaQuantitat) {
        return TerminiResponseImpl.builder()
                .from(this)
                .quantitatAnterior(this.quantitat())
                .quantitat(novaQuantitat)
                .quantitatRebuda(0L)
                .build();
    }

    default TerminiResponse aplicaEntrada(long quantitatEntrada) {
        var quantitatSumada = quantitatRebuda() + quantitatEntrada;
        var quantitatFinal = Math.min(quantitatSumada, quantitat());
        return TerminiResponseImpl.builder()
                .from(this)
                .quantitatRebuda(quantitatFinal)
                .build();
    }
}
