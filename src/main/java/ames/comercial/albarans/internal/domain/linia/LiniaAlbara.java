package ames.comercial.albarans.internal.domain.linia;

import ames.comercial.albarans.AlbaransException.QuantitatPendentFacturarInvalida;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniaAlbaraImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaAlbara {

    KeyLiniaAlbara id();
    KeyArticleClient articleClient();
    InformacioPesa informacioPesa();
    Optional<InformacioComanda> infoComanda();  // En els albarans de traspàs no hi ha informació de comanda associada
    long quantitat();
    Preu preu();
    boolean isPreuFixat();
    /**
     * Descompte comercial en % que s'aplica sobre el preu de la línia; 0 si no n'hi ha.
     * <p>
     * El preu que es desa a la línia és sempre el brut de tarifa: el descompte de normalitzats i
     * filtres (per família de l'article i percentatges del client, veure {@code CalculDescompteFamilia})
     * viatja a part. És una foto del descompte que tenia la línia de comanda en el moment de servir-la,
     * de manera que un canvi posterior dels percentatges del client no altera els albarans ja fets.
     * S'aplica també quan el preu està fixat manualment ({@link #isPreuFixat()}).
     * <p>
     * Als albarans de traspàs i de consum sempre és 0 perquè no hi ha línia de comanda al darrere.
     */
    @Value.Default
    default BigDecimal descompte() { return BigDecimal.ZERO; }
    long quantitatPendentFacturar();
    long quantitatPendentConsumir();
    Optional<Long> comandaBlanca();
    Optional<String> observacionsImpressio();
    Optional<String> observacionsInternes();
    Optional<String> codiEmbalatge();
    // Identificador opcional del consum (només s'informa a les línies dels albarans de consum)
    Optional<String> identificadorConsum();
    @Value.Default default boolean isUrgent() { return false; };

    // TODO Eliminar després de migració, només necessari per mantenir la data de registre original en les línies migrades
    Optional<LocalDate> dataregMigracio();
    // TODO Eliminar després de migració, només necessari per mantenir l'usuari de registre original en les línies migrades
    Optional<String> usuregMigracio();

    // Import brut de la línia: preu × quantitat, sense aplicar-hi el descompte
    @Value.Derived
    default BigDecimal importBrut() {
        return preu().impBrut(quantitat());
    }

    /**
     * Import net de la línia: l'import brut amb el {@link #descompte()} aplicat. És el que s'acabarà
     * facturant i el que es mostra a la columna d'import del detall i del buscador.
     */
    @Value.Derived
    default BigDecimal importNet() {
        return preu().impNet(quantitat(), descompte());
    }

    /**
     * Divisa en què s'expressa l'import de la línia: la base del preu. Cal distingir-la de la del preu
     * perquè els articles tarifats per cèntims tenen el preu en {@code EUR%} però l'import en {@code EUR}
     * ({@link Preu#imp(long)} ja hi fa la conversió).
     */
    @Value.Derived
    default Divisa divisaImport() {
        return preu().divisa().base();
    }

    /** Cert si la línia ja s'ha començat a facturar, encara que sigui parcialment. */
    @Value.Derived
    default boolean isFacturacioIniciada() {
        return quantitatPendentFacturar() < quantitat();
    }

    /** Pes total de la línia en Kg: pes unitari de la peça (g) × quantitat servida. */
    @Value.Derived
    default BigDecimal pesTotal() {
        return InformacioPesa.calcularPesKg(informacioPesa().pesUnitari(), quantitat());
    }

    default LiniaAlbara incrementarQuantitat(long quantitat) {
        return incrementarQuantitat(quantitat, true, false);
    }

    /**
     * Incrementa la quantitat de la línia. El pendent de facturar només creix si la línia es factura:
     * a un traspàs sense canvi d'empresa les línies neixen amb pendent de facturar zero i s'hi ha de
     * quedar encara que després se n'incrementi la quantitat.
     * <p>
     * El pendent de consumir segueix el mateix criteri: només creix a les línies dels traspassos cap a
     * una plataforma marcada per al SII, que són les úniques que en neixen amb.
     */
    default LiniaAlbara incrementarQuantitat(long quantitat, boolean isFacturable, boolean isConsumible) {
        return LiniaAlbaraImpl.builder()
                .from(this)
                .quantitat(this.quantitat() + quantitat)
                .quantitatPendentFacturar(isFacturable
                        ? this.quantitatPendentFacturar() + quantitat
                        : this.quantitatPendentFacturar())
                .quantitatPendentConsumir(isConsumible
                        ? this.quantitatPendentConsumir() + quantitat
                        : this.quantitatPendentConsumir())
                .build();
    }

    default LiniaAlbara facturar(long quantitatFacturar) {
        return LiniaAlbaraImpl.builder()
                .from(this)
                .quantitatPendentFacturar(Math.max(quantitatPendentFacturar() - quantitatFacturar, 0))
                .build();
    }

    default LiniaAlbara desferFacturar(long quantitatDesferFacturar) {
        return LiniaAlbaraImpl.builder()
                .from(this)
                .quantitatPendentFacturar(Math.min(quantitatPendentFacturar() + quantitatDesferFacturar, quantitat()))
                .build();
    }

    /**
     * Rectifica manualment la quantitat pendent de facturar de la línia. A diferència de
     * {@link #facturar(long)} i {@link #desferFacturar(long)}, que hi apliquen un increment com a
     * conseqüència d'una factura, aquí es fixa directament el valor. No pot ser negativa ni superar
     * la quantitat servida a la línia.
     */
    default LiniaAlbara canviarQuantitatPendentFacturar(long quantitatPendentFacturar) {
        if (quantitatPendentFacturar < 0 || quantitatPendentFacturar > quantitat()) {
            throw new QuantitatPendentFacturarInvalida(id(), quantitatPendentFacturar, quantitat());
        }
        return LiniaAlbaraImpl.builder()
                .from(this)
                .quantitatPendentFacturar(quantitatPendentFacturar)
                .build();
    }

    default LiniaAlbara consumir(long quantitatConsumir) {
        return LiniaAlbaraImpl.builder()
                .from(this)
                .quantitatPendentConsumir(Math.max(quantitatPendentConsumir() - quantitatConsumir, 0))
                .build();
    }

    default LiniaAlbara desferConsumir(long quantitatDesferConsumir) {
        return LiniaAlbaraImpl.builder()
                .from(this)
                .quantitatPendentConsumir(Math.min(quantitatPendentConsumir() + quantitatDesferConsumir, quantitat()))
                .build();
    }

    /**
     * Marcar/Desmarcar una linia com a urgent
     */

    default LiniaAlbara marcarLiniaUrgentNoUrgent(boolean urgent){
        return LiniaAlbaraImpl.builder()
                .from(this)
                .isUrgent(urgent)
                .build();
    }
}

