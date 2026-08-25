package ames.permisos.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.internal.$processor$.meta.$ValueMirrors.Derived;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static ames.permisos.shared.Numbers.decimal;

@JsonDeserialize(builder = PreuImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Preu {

    BigDecimal valor();
    Divisa divisa();

    static Preu of (BigDecimal valor, Divisa divisa) {
        return PreuImpl.builder()
                .valor(valor)
                .divisa(divisa)
                .build();
    }

    static Preu of (BigDecimal valor, String divisa) {
        return PreuImpl.builder()
                .valor(valor)
                .divisa(Divisa.getBySymbol(divisa))
                .build();
    }

    default boolean isEquals(Object p) {
        if (p == null)
            return false;
        if (!(p instanceof Preu pre))
            return false;
        return pre.valor().compareTo(valor())==0 && pre.divisa().equals(divisa());
    }

    static boolean mateix(Preu a, Preu b) {
        return a.isEquals(b);
    }

    @Derived
    default BigDecimal imp (long unitats) {
        // Si la divisa no es per cèntims (%) retornem el valor multiplicat per les unitats
        if (!divisa().isCent())
            return valor().multiply(BigDecimal.valueOf(unitats));
        // Si la divisa és cent (%) cal dividir entre 100 el valor
        return valor().multiply(BigDecimal.valueOf(unitats))
                .divide(decimal(100), 3, RoundingMode.HALF_UP);
    }

    /** Import brut d'una quantitat: {@link #imp(long)} arrodonit a 2 decimals. */
    default BigDecimal impBrut (long unitats) {
        return imp(unitats).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Import net d'una quantitat: l'import brut de {@link #imp(long)} amb el descompte comercial (%)
     * aplicat, arrodonit a 2 decimals.
     * <p>
     * És l'únic punt on es calcula el descompte sobre un import, perquè no se n'hagi de replicar la
     * fórmula a cada línia (albarà, cerca d'albarans i propostes). Les línies de comanda i el càlcul
     * de normalitzats <b>no</b> el fan servir: arrodoneixen a 3 decimals a propòsit, perquè el seu
     * import de línia és un valor intermedi que després se suma i s'arrodoneix un sol cop al total.
     */
    default BigDecimal impNet (long unitats, BigDecimal descompte) {
        return imp(unitats)
                .multiply(Numbers.descompteAplicar(descompte))
                .divide(decimal(100), 2, RoundingMode.HALF_UP);
    }

}
