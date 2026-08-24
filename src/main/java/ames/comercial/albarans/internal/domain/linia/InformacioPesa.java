package ames.comercial.albarans.internal.domain.linia;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@JsonDeserialize(builder = InformacioPesaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioPesa {

    String matriu();
    String referencia();
    String nivellTecnic();
    String denominacio();
    String codiPartidaArantzelaria();
    String partidaArantzelaria();
    Optional<String> codiEan13();
    boolean isVolUdi();
    int diesCaducitat();
    Optional<String> codiFamilia();
    /** Pes unitari de la peça en grams (Advantage {@code ART.ARTPFIN}) */
    BigDecimal pesUnitari();
    /**
     * Unitats del nivell base d'embalatge (Advantage {@code ACLUCAI}); 0 si no n'hi ha.
     * <p>
     * Atenció: el nivell que representa depèn del tipus d'article. Als <b>no normalitzats</b> són les
     * peces per <b>caixa</b> (és el que valida {@code TestQEmb} del legacy, replicat a
     * {@link ames.comercial.albarans.internal.services.CalcularAvisosEmbalatge}). Als
     * <b>normalitzats</b> són les peces per <b>bossa</b>, i les peces per caixa surten de
     * multiplicar-les per {@link #bossesCaixa()}.
     */
    long unitatsEmbalatge();
    /**
     * Bosses per caixa de l'article-client (Advantage {@code BOSXCAI}); 0 si no n'hi ha.
     * Només està informat als normalitzats i és merament informatiu: cap validació d'albarans l'usa.
     */
    long bossesCaixa();
    /**
     * Caixes per palet de l'article-client (Advantage {@code ACLUCAP}); 0 si no n'hi ha.
     * Als normalitzats sempre és 0 perquè no se'ls controla el palet.
     */
    long caixesPalet();

    /**
     * Peces per palet: el condicionament complet ({@link #unitatsEmbalatge()} × {@link #caixesPalet()}).
     * És 0 quan qualsevol dels dos factors no està informat, és a dir quan no hi ha condicionament
     * complet definit (cas de tots els normalitzats). No es persisteix perquè és el producte exacte
     * dels altres dos, el mateix criteri que fa servir
     * {@link ames.comercial.albarans.internal.services.CalcularAvisosEmbalatge}.
     */
    @Value.Derived
    default long unitatsPalet() {
        return unitatsEmbalatge() * caixesPalet();
    }

    /**
     * Pes en Kg d'una quantitat de peces a partir del pes unitari en grams. Únic punt de càlcul del
     * mòdul, perquè el frontend no hagi de replicar la fórmula (mateix criteri que
     * {@code RegArticlesPropostesClient.calcularPes} al mòdul de propostes).
     */
    static BigDecimal calcularPesKg(BigDecimal pesUnitariGrams, long quantitat) {
        return pesUnitariGrams
                .multiply(BigDecimal.valueOf(quantitat))
                .divide(BigDecimal.valueOf(1_000), 2, RoundingMode.HALF_UP);
    }

    /**
     * Factory amb els camps "pesa" bàsics. Els camps addicionals
     */
    // TODO Eliminar després de migració
    static InformacioPesa empty(){
        return InformacioPesaImpl.builder()
                .matriu("")
                .referencia("")
                .nivellTecnic("")
                .denominacio("")
                .codiPartidaArantzelaria("")
                .partidaArantzelaria("")
                .codiEan13(Optional.empty())
                .isVolUdi(false)
                .diesCaducitat(0)
                .codiFamilia(Optional.empty())
                .pesUnitari(BigDecimal.ZERO)
                .unitatsEmbalatge(0)
                .bossesCaixa(0)
                .caixesPalet(0)
                .build();
    }

}

