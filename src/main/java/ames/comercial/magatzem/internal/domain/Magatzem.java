package ames.comercial.magatzem.internal.domain;

import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Magatzem (dades mestres). Substitueix el model {@code MagatzemAds} que es llegia de la taula
 * {@code comundb.mag} de l'Advantage; ara les dades viuen a {@code com_magatzem.magatzem} (PostgreSQL).
 */
@JsonDeserialize(builder = MagatzemImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Magatzem {

    String codi();
    TipusMagatzem tipus();
    String descripcio();
    Adresa adresa();
    Optional<InformacioEnviament> informacioEnviament();
    long diesTransport();

    /**
     * TODO Revisar per eliminar quan l'alta d'albarans de traspàs estigui provada.
     * <p>
     * Ja no la fa servir cap decisió de negoci: si un traspàs es factura o no ho diu ara
     * {@code Albara.isTraspasEmpresa()}, que es fixa en crear la capçalera segons si hi ha canvi d'empresa
     * (no s'hi enllaça per no acoblar aquest mòdul amb el d'albarans). Es manté perquè encara no es
     * descarta que els usuaris demanin poder marcar un magatzem com a facturable dins de la mateixa
     * empresa; si no és el cas, cal treure-la també de {@code com_magatzem.magatzem}
     * ({@code is_facturable}), de {@code MagatzemRepositorySQL} i del manteniment de magatzems.
     */
    boolean isFacturable();
    boolean isActiu();
    Optional<String> usuariResponsable();

    /**
     * Cert si les sortides de mercaderia del magatzem s'han de declarar al SII (Suministro Inmediato de
     * Información) de la hisenda pública. Només té sentit als magatzems de plataforma: és el que decideix
     * si les seves línies de traspàs mantenen pendent de consumir i si els consums i els retorns hi deixen
     * apunt a {@code albarans.sortides_plataforma}, que és el que llegeix el procés que genera els fitxers
     * del SII. Als magatzems no marcats els consums es fan igualment, però no descompten pendent ni deixen
     * cap apunt.
     */
    @Value.Default
    default boolean isSii() {
        return false;
    }

    default boolean isPlataforma() {
        return tipus() == TipusMagatzem.PLATAFORMA;
    }

    /**
     * Cert si el magatzem és controlat internament ({@link TipusMagatzem#AMES} o
     * {@link TipusMagatzem#TRANSIT}), en contraposició als de plataforma. Els magatzems controlats
     * internament no poden obrir caixes, per això la quantitat a servir hauria de ser múltiple de les
     * unitats d'embalatge (veure {@link ames.comercial.albarans.internal.services.CalcularAvisosEmbalatge}).
     */
    default boolean isControlatInternament() {
        return tipus() != TipusMagatzem.PLATAFORMA;
    }

}
