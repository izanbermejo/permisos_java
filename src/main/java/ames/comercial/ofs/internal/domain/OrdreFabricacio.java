package ames.comercial.ofs.internal.domain;

import ames.comercial.ofs.internal.domain.service.AplicarEntradaTerminis;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.OFExceptions;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = OrdreFabricacioImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface OrdreFabricacio {

    long numero();
    KeyArticleClient articleClient();
    String fabrica();
    LocalDate dataEmissio();
    Optional<Long> ofAnterior();
    Optional<Long> ofPosterior();
    List<Termini> terminis();
    long quantitatRebudaEntrades();
    long quantitatRebudaEntradesAcumulatTotal();
    long ultimaQuantitatRebuda();
    long ultimaQuantitatRebudaAbansCreacio();
    Optional<LocalDate> dataUltimaQuantitatRebuda();
    Optional<LocalDate> dataUltimaQuantitatRebudaAbansCreacio();
    Optional<LocalDate> dataAnulacio();
    long diesCalculIncrement();
    boolean canviFabrica();

    @Derived default long quantitatTotal() {
        return terminis().stream().mapToLong(Termini::quantitat).sum();
    }

    @Derived default long quantitatRebudaTerminis() {
        return terminis().stream().mapToLong(Termini::quantitatRebuda).sum();
    }

    @Derived default long quantitatPendent() {
        return Math.max(0,  quantitatTotal() - quantitatRebudaEntrades());
    }

    @Derived default long exces() {
        return Math.max(0,  quantitatRebudaEntrades() - quantitatRebudaTerminis());
    }

    @Derived default boolean isAnulada() {
        return dataAnulacio().isPresent();
    }

    @Derived default boolean isFinalitzada() {
        return quantitatPendent() == 0;
    }

    default OrdreFabricacio aplicaEntrada(long quantitat) {
        // En cas que sigui més d'una entrada en el mateix dia la quantitat s'acumula
        var qtatCalculada = quantitat;
        Optional<LocalDate> dataOpt = dataUltimaQuantitatRebuda();
        if (dataOpt.isPresent() && dataOpt.get().equals(LocalDate.now())) {
            qtatCalculada += ultimaQuantitatRebuda();
        }

        return OrdreFabricacioImpl.builder()
                .from(this)
                .quantitatRebudaEntrades(quantitatRebudaEntrades()+quantitat)
                .ultimaQuantitatRebuda(qtatCalculada)
                .dataUltimaQuantitatRebuda(LocalDate.now())
                .terminis(new AplicarEntradaTerminis(terminis()).executar(quantitat))
                .build();
    }

    default OrdreFabricacio tancar(long novaOf) {
        return OrdreFabricacioImpl.builder()
                .from(this)
                .ofPosterior(novaOf)
                .build();
    }

    default OrdreFabricacio anular(){
        if (!articleClient().isNormalitzat()) throw new OFExceptions.OFNoNormalitzada(numero());
        if (ofPosterior().isPresent()) throw new OFExceptions.NoEsUltimaOF(numero());
        if (isAnulada()) throw new OFExceptions.OFEstaAnulada();
        return OrdreFabricacioImpl.builder()
                .from(this)
                .dataAnulacio(LocalDate.now())
                .build();
    }

    @Derived default boolean existeixIncrement() {
        if (diesCalculIncrement() <= 0)
            return false;
        // El dia límit a tenir en compte per veure si hi ha increment és el dia d'avui
        // sumant els dies parametritzats. Per exemple si avui es 1/1 i el paràmetre
        // és 1 s'hauria de mirar l'increment fins el dia 2/1 inclòs
        var diaLimit = LocalDate.now().plusDays(diesCalculIncrement());
        var quantitatAnterior = terminis().stream()
                .filter(t -> !t.data().isAfter(diaLimit))
                .mapToLong(Termini::quantitatAnterior)
                .sum();
        var quantitatActual = terminis().stream()
                .filter(t -> !t.data().isAfter(diaLimit))
                .mapToLong(Termini::quantitat)
                .sum();
        return quantitatActual > quantitatAnterior;
    }

    /**
     * Calcula el número d'OF anterior a enviar a fàbrica. En cas que hagi canvi de fàbrica
     * s'envia 0 en comptes de l'OF anterior
     *
     * @return Long amb el número d'OF anterior a mostrar en el fitxer que s'envia a fàbrica
     */
    @Derived default Long ofAnteriorFitxerFabrica() {
        if (canviFabrica())
            return 0L;
        return ofAnterior().orElse(0L);
    }

    /**
     * Creació d'una OF especial a partir d'una existent. La nova OF tindrà:
     * - Com a OF anterior l'OF que es passa per paràmetre
     * - La quantitat rebuda total acumulada serà la suma de la acumulada quan es va crear mes la rebuda per entrades
     * - L'última quantitat rebuda serà l'última rebuda si s'ha rebut alguna o la d'abans de la creació
     * - L'última data de la quantitat rebuda serà l'última rebuda si s'ha rebut alguna o la d'abans de la creació
     * - L´'ultima quantitat rebuda abans de la creació serà l'ultima quantitat rebuda de l'OF que es passa per paràmetre
     * - L'última data de la quantitat rebuda serà l'última data de la quantitat rebuda de l'OF que es passa per paràmetre
     *
     * @param of OF que serà anterior a la nova
     * @param nouNumero Nou número d'OF
     * @param terminis Nous terminis que tindrà la nova OF
     * @return Nova OF
     */
    static OrdreFabricacio fromEspecial(OrdreFabricacio of, long nouNumero, String fabrica, List<Termini> terminis, long diesIncrementComandes) {
        return OrdreFabricacioImpl.builder()
                .from(of)
                .numero(nouNumero)
                .fabrica(fabrica)
                .dataEmissio(LocalDate.now())
                .ofAnterior(of.numero())
                .ofPosterior(Optional.empty())
                .terminis(terminis)
                .diesCalculIncrement(diesIncrementComandes)
                .quantitatRebudaEntrades(0)
                .ultimaQuantitatRebuda(0)
                .dataUltimaQuantitatRebuda(Optional.empty())
                .ultimaQuantitatRebudaAbansCreacio(of.ultimaQuantitatRebuda() > 0 ? of.ultimaQuantitatRebuda() : of.ultimaQuantitatRebudaAbansCreacio())
                .dataUltimaQuantitatRebudaAbansCreacio(of.dataUltimaQuantitatRebuda().or(of::dataUltimaQuantitatRebudaAbansCreacio))
                .quantitatRebudaEntradesAcumulatTotal(of.quantitatRebudaEntradesAcumulatTotal() + of.quantitatRebudaEntrades())
                .dataAnulacio(Optional.empty())
                .canviFabrica(!fabrica.equals(of.fabrica()))    // Es comprova si la fàbrica ha canviat respecte l'OF anterior
                .build();
    }

    static OrdreFabricacio from(long nouNumero, KeyArticleClient articleClient, String fabrica, List<Termini> terminis) {
        return OrdreFabricacioImpl.builder()
                .numero(nouNumero)
                .articleClient(articleClient)
                .fabrica(fabrica)
                .dataEmissio(LocalDate.now())
                .ofAnterior(Optional.empty())
                .ofPosterior(Optional.empty())
                .terminis(terminis)
                .diesCalculIncrement(0)
                .quantitatRebudaEntrades(0)
                .ultimaQuantitatRebuda(0)
                .dataUltimaQuantitatRebuda(Optional.empty())
                .ultimaQuantitatRebudaAbansCreacio(0)
                .dataUltimaQuantitatRebudaAbansCreacio(Optional.empty())
                .quantitatRebudaEntradesAcumulatTotal(0)
                .dataAnulacio(Optional.empty())
                .canviFabrica(false)
                .build();
    }

}
