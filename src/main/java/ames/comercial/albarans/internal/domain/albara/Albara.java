package ames.comercial.albarans.internal.domain.albara;

import ames.comercial.albarans.AlbaransException.*;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = AlbaraImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Albara {

    KeyAlbara id();
    TipusAlbara tipus();
    // Els albarans de traspàs no tenen client ja que es tracta d'un moviment intern
    Optional<String> client();
    LocalDate data();
    String magatzem();
    Adresa adresa();
    InformacioEnviament informacioEnviament();
    Optional<String> destiAlternatiu();
    Optional<Adresa> adresaBroker();
    Optional<Adresa> adresaFacturaProforma();
    InformacioMagatzem informacioMagatzem();
    Optional<InformacioTraspas> informacioTraspas();
    Optional<InformacioEdi> informacioEdi();
    Optional<CostLogistic> costLogistic();
    Optional<CostTransport> costTransport();
    Optional<CostEnviamentExpress> costEnviamentExpress();
    Optional<String> observacionsImpressio();
    Optional<String> observacionsInternes();
    Optional<String> observacionsProforma();
    Optional<String> incidenciaTransport();
    LocalDate dataCreacio();
    String usuariCreacio();
    @Default default boolean isUrgent() { return  false; }
    Optional<String> numeroProveidor();
    Optional<String> numeroAlbaraEspecial();
    @Default default boolean isTancat() { return  false; }
    @Default default boolean isFacturat() { return  false; }
    @Default default boolean isFacturacioAutomatica() { return  false; }
    @Default default boolean isEnviatEmail() { return  false; }
    @Default default boolean isNormalitzats() { return  false; }
    @Default default boolean isCalPagarPorts() { return  false; }
    @Default default boolean isNoValorat() { return  false; }
    @Default default boolean isEnviatHisenda() { return  false; }
    Optional<String> numeroFacturaTransport();
    Optional<Long> numeroCaixes();
    Optional<String> alsadaCaixes();
    Optional<BigDecimal> costMoq();
    Optional<String> referenciaTransport();

    @Derived
    default boolean isTraspas() { return informacioTraspas().isPresent();}

    @Derived
    default String empresaTraspas() { return informacioTraspas().map(InformacioTraspas::empresaReceptora).orElse(""); }

    @Derived
    default String magatzemTraspas() { return informacioTraspas().map(InformacioTraspas::magatzemReceptor).orElse(""); }

    /**
     * Cert si el traspàs creua empreses. Un traspàs entre magatzems de la mateixa empresa és un moviment
     * intern i no es factura; un que canvia d'empresa sí, i per això les seves línies neixen amb quantitat
     * pendent de facturar i es valoren amb la tarifa AMES.
     * <p>
     * Queda determinat en crear la capçalera (veure {@code CrearCapsaleraAlbaraTraspas}, que en dedueix
     * també el {@link TipusAlbara}), de manera que les línies que s'hi afegeixin després ho han de llegir
     * d'aquí i no tornar-ho a deduir del magatzem de destí.
     */
    @Derived
    default boolean isTraspasEmpresa() {
        return isTraspas() && !id().empresa().equals(empresaTraspas());
    }

    /**
     * Cert si el traspàs s'ha marcat com a abonable. En comptes de facturar-lo, l'empresa de destí en rep
     * un abonament: la creació de cada línia genera un pendent d'abonar a {@code penabo} de l'Advantage,
     * que és la taula que consumeix el mòdul de facturació. Només té sentit amb canvi d'empresa, perquè és
     * l'únic cas en què el traspàs es facturaria.
     */
    @Derived
    default boolean isTraspasAbonable() {
        return informacioTraspas().map(InformacioTraspas::isTraspasAbonable).orElse(false);
    }

    /**
     * Cert si les línies del traspàs neixen amb quantitat pendent de facturar i valorades amb la tarifa
     * AMES. Es factura quan creua empreses, tret que s'hagi marcat com a abonable: aleshores la
     * contrapartida econòmica és un abonament i no una factura, i les dues coses s'exclouen.
     */
    @Derived
    default boolean isTraspasFacturable() {
        return isTraspasEmpresa() && !isTraspasAbonable();
    }

    /**
     * Comprova que es pot canviar el flag d'abonable. Només en traspassos amb canvi d'empresa (a la
     * resta el flag no faria res) i mentre no s'hagi començat a facturar: el canvi mou el pendent de
     * facturar de les línies i els registres de {@code penabo}, i cap de les dues coses es pot desfer
     * un cop la facturació hi ha entrat.
     */
    default void checkPotCanviarTraspasAbonable() {
        if (!isTraspasEmpresa()) {
            throw new TraspasNoAbonable(id());
        }
        if (isFacturat()) {
            throw new TraspasAbonableFacturacioIniciada(id());
        }
    }

    default Albara canviarTraspasAbonable(boolean isTraspasAbonable) {
        checkPotCanviarTraspasAbonable();
        return AlbaraImpl.builder()
                .from(this)
                .informacioTraspas(InformacioTraspasImpl.builder()
                        .from(informacioTraspas().orElseThrow())
                        .isTraspasAbonable(isTraspasAbonable)
                        .build())
                .build();
    }

    default Albara canviarFacturat(boolean isFacturat) {
        return AlbaraImpl.builder()
                .from(this)
                .isFacturat(isFacturat)
                .build();
    }

    /**
     * Comprova que es pot canviar el flag d'autofacturable. Només té sentit als albarans de client:
     * els de traspàs i els de consum de plataforma no entren mai al procés de facturació automàtica.
     * A més, un albarà que ja s'ha començat a facturar no es pot canviar; aquí només es valida el
     * flag de capçalera, perquè la comprovació de les línies ja facturades la fa el command
     * {@code CanviarFacturacioAutomaticaAlbara}, que és qui té accés a les línies.
     */
    default void checkPotCanviarFacturacioAutomatica() {
        if (tipus() != TipusAlbara.CLIENT) {
            throw new AlbaraNoAutofacturable(id());
        }
        if (isFacturat()) {
            throw new AlbaraFacturatNoCanviarAutofacturable(id());
        }
    }

    default Albara canviarFacturacioAutomatica(boolean isFacturacioAutomatica) {
        checkPotCanviarFacturacioAutomatica();
        return AlbaraImpl.builder()
                .from(this)
                .isFacturacioAutomatica(isFacturacioAutomatica)
                .build();
    }

    default Albara tancar() {
        return AlbaraImpl.builder()
                .from(this)
                .isTancat(true)
                .build();
    }

    /**
     * Comprova que l'albarà no s'hagi presentat ja al SII de la hisenda pública. El procés que genera
     * els fitxers del SII marca els albarans que ha declarat ({@link #isEnviatHisenda()}) i, a partir
     * d'aquell moment, ni es poden modificar ni eliminar: el que s'ha declarat ha de continuar existint
     * tal com es va declarar.
     */
    default void checkNoPresentatHisenda() {
        if (isEnviatHisenda()) {
            throw new AlbaraPresentatHisenda(id());
        }
    }

    /**
     * Comprova que es poden modificar les línies de l'albarà (afegir-ne, eliminar-ne o reobrir-lo).
     * No es permet si l'albarà està facturat, si té algun moviment de magatzem actiu o si ja s'ha
     * presentat al SII, amb missatges d'error diferenciats per cada cas.
     */
    default void checkPotModificarLinies() {
        if (isFacturat()) {
            throw new AlbaraFacturat(id());
        }
        if (informacioMagatzem().teMovimentMagatzem()) {
            throw new AlbaraAmbMovimentMagatzem(id());
        }
        checkNoPresentatHisenda();
    }

    default Albara reobrir() {
        checkPotModificarLinies();
        return AlbaraImpl.builder()
                .from(this)
                .isTancat(false)
                .build();
    }

    default Albara entregar() {
        return AlbaraImpl.builder()
                .from(this)
                .informacioMagatzem(informacioMagatzem().entregar())
                .build();
    }

    default Albara servir() {
        return AlbaraImpl.builder()
                .from(this)
                .informacioMagatzem(informacioMagatzem().servir())
                .build();
    }

    default Albara desferServir() {
        return AlbaraImpl.builder()
                .from(this)
                .informacioMagatzem(informacioMagatzem().desferServir())
                .build();
    }

    default Albara enServei() {
        return AlbaraImpl.builder()
                .from(this)
                .informacioMagatzem(informacioMagatzem().enServei())
                .build();
    }

    default Albara enPreparacio() {
        return AlbaraImpl.builder()
                .from(this)
                .informacioMagatzem(informacioMagatzem().enPreparacio())
                .build();
    }

    default Albara desferEnPreparacio() {
       return AlbaraImpl.builder()
               .from(this)
               .informacioMagatzem(informacioMagatzem().desferEnPreparacio())
               .build();
    }

    default Albara actualitzarInformacioMagatzem(InformacioMagatzem novaInformacioMagatzem) {
        return AlbaraImpl.builder()
                .from(this)
                .informacioMagatzem(novaInformacioMagatzem)
                .build();
    }

    /**
     * Comprova que es pot modificar l'adreça de l'albarà. Només es permet mentre l'albarà està obert;
     * un albarà tancat (i per tant també facturat, ja que la facturació implica el tancament) no es pot editar.
     */
    default void checkPotCanviarAdresa() {
        if (isTancat()) {
            throw new AlbaraTancatNoEditar(id());
        }
    }

    default Albara canviarAdresa(Adresa adresa, InformacioEnviament informacioEnviament) {
        checkPotCanviarAdresa();
        return AlbaraImpl.builder()
                .from(this)
                .adresa(adresa)
                .informacioEnviament(informacioEnviament)
                .build();
    }

    default Albara canviarAdresaBroker(Optional<Adresa> adresaBroker) {
        checkPotCanviarAdresa();
        return AlbaraImpl.builder()
                .from(this)
                .adresaBroker(adresaBroker)
                .build();
    }

    default Albara canviarAdresaFacturaProforma(Optional<Adresa> adresaFacturaProforma) {
        checkPotCanviarAdresa();
        return AlbaraImpl.builder()
                .from(this)
                .adresaFacturaProforma(adresaFacturaProforma)
                .build();
    }

    /**
     * Canvia el número d'albarà especial. Només es permet mentre l'albarà està obert (un albarà tancat,
     * i per tant també facturat, no es pot editar).
     */
    default Albara canviarNumeroAlbaraEspecial(Optional<String> numeroAlbaraEspecial) {
        checkPotCanviarAdresa();
        return AlbaraImpl.builder()
                .from(this)
                .numeroAlbaraEspecial(numeroAlbaraEspecial)
                .build();
    }

    /**
     * Comprova que es pot canviar la data de l'albarà. Només es permet mentre està obert: la data es
     * copia als moviments d'inventari i a la traçabilitat en afegir cada línia, i un albarà tancat
     * (i per tant també facturat) ja no es pot editar.
     * <p>
     * En els traspassos abonables aquesta restricció és, a més, el que garanteix que no es pugui perdre
     * cap abonament parcial: la data forma part de la clau dels pendents d'abonar a {@code penabo} i
     * canviar-la obliga a refer-los. Un albarà no s'abona fins que s'ha tancat i servit, i aleshores ja
     * no es pot reobrir, de manera que un albarà obert no pot tenir cap abonament començat.
     */
    default void checkPotCanviarData() {
        if (isTancat()) {
            throw new AlbaraTancatNoCanviarData(id());
        }
        // La data es propaga a la traçabilitat de sortides_plataforma, que és el que declara el SII
        checkNoPresentatHisenda();
    }

    default Albara canviarData(LocalDate data) {
        checkPotCanviarData();
        return AlbaraImpl.builder()
                .from(this)
                .data(data)
                .build();
    }

    /**
     * Marcar/Desmarcar un albara com a urgent
     */
    default Albara marcarUrgentNoUrgent(Optional<CostEnviamentExpress> costEnviamentExpress, boolean urgent){
        return AlbaraImpl.builder()
                .from(this)
                .isUrgent(urgent)
                .costEnviamentExpress(costEnviamentExpress)
                .build();
    }

}
