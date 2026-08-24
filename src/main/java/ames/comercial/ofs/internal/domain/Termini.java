package ames.comercial.ofs.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;

@JsonDeserialize(builder = TerminiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Termini {

    LocalDate data();
    LocalDate dataSortida();
    long quantitat();
    long quantitatAnterior();
    long quantitatRebuda();
    boolean isStockSeguretat();
    @Derived default long quantitatPendent() { return Math.max(0, quantitat() - quantitatRebuda()); }
    @Derived default boolean isPendent() {
        return quantitatPendent() > 0;
    }
    @Derived default boolean hasQuantitat() {
        return quantitat() > 0;
    }

    static Termini nou(long quantitat, LocalDate data) {
        return nou(quantitat, false, data, data);
    }

    static Termini nou(long quantitat, boolean isStockSeguretat, LocalDate dataClient, LocalDate dataSortida) {
        return TerminiImpl.builder()
                .data(dataClient)
                .dataSortida(dataSortida)
                .quantitat(quantitat)
                .quantitatAnterior(0)
                .quantitatRebuda(0)
                .isStockSeguretat(isStockSeguretat)
                .build();
    }

    default Termini nouAnterior() {
        return TerminiImpl.builder()
                .from(this)
                .quantitatAnterior(this.quantitat())
                .quantitat(this.quantitatPendent())
                .quantitatRebuda(0L)
                .build();
    }

    default Termini nouAnterior(long novaQuantitat) {
        return TerminiImpl.builder()
                .from(this)
                .quantitatAnterior(this.quantitat())
                .quantitat(novaQuantitat)
                .quantitatRebuda(0L)
                .build();
    }

    default Termini aplicaEntrada(long quantitatEntrada) {
        var quantitatSumada = quantitatRebuda() + quantitatEntrada;
        var quantitatFinal = Math.min(quantitatSumada, quantitat());
        return TerminiImpl.builder()
                .from(this)
                .quantitatRebuda(quantitatFinal)
                .build();
    }

}
