package ames.comercial.ofs.response;

import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ObtenirOFResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ObtenirOFResponse {

    long numero();
    KeyArticleClient articleClient();
    String fabrica();
    LocalDate dataEmissio();
    Optional<Long> ofAnterior();
    Optional<Long> ofPosterior();
    List<TerminiResponse> terminis();
    long quantitatRebudaEntrades();
    long quantitatRebudaEntradesAcumulatTotal();
    long ultimaQuantitatRebuda();
    long ultimaQuantitatRebudaAbansCreacio();
    Optional<LocalDate> dataUltimaQuantitatRebuda();
    Optional<LocalDate> dataUltimaQuantitatRebudaAbansCreacio();
    Optional<LocalDate> dataAnulacio();
    Optional<Boolean> increment();
    Optional<Boolean> canviFabrica();
    Optional<Integer> diesCalculIncrement();

    @Value.Derived
    default long quantitatTotal() {
        return terminis().stream().mapToLong(TerminiResponse::quantitat).sum();
    }

    @Value.Derived
    default long quantitatRebudaTerminis() {
        return terminis().stream().mapToLong(TerminiResponse::quantitatRebuda).sum();
    }

    @Value.Derived
    default long quantitatPendent() {
        return Math.max(0,  quantitatTotal() - quantitatRebudaEntrades());
    }

    @Value.Derived
    default long exces() {
        return Math.max(0,  quantitatRebudaEntrades() - quantitatRebudaTerminis());
    }

    @Value.Derived
    default boolean isAnulada() {
        return dataAnulacio().isPresent();
    }

    @Value.Derived
    default boolean isFinalitzada() {
        return quantitatPendent() == 0;
    }

    @Value.Derived
    default ObtenirOFResponse tancar(long novaOf) {
        return ObtenirOFResponseImpl.builder()
                .from(this)
                .ofPosterior(novaOf)
                .build();
    }

    @Value.Derived
    default Optional<LocalDate> dataLimitIncrement() {
        if (diesCalculIncrement().isEmpty()) return Optional.empty();
        return Optional.of(dataEmissio().plusDays(diesCalculIncrement().get()));
    }

    @Value.Derived
    default boolean showIncrement() {
        return increment().orElse(false);
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
    static ObtenirOFResponse fromEspecial(ObtenirOFResponse of, long nouNumero, List<TerminiResponse> terminis) {
        return ObtenirOFResponseImpl.builder()
                .from(of)
                .numero(nouNumero)
                .dataEmissio(LocalDate.now())
                .ofAnterior(of.numero())
                .ofPosterior(Optional.empty())
                .terminis(terminis)
                .quantitatRebudaEntrades(0)
                .ultimaQuantitatRebuda(0)
                .dataUltimaQuantitatRebuda(Optional.empty())
                .ultimaQuantitatRebudaAbansCreacio(of.ultimaQuantitatRebuda() > 0 ? of.ultimaQuantitatRebuda() : of.ultimaQuantitatRebudaAbansCreacio())
                .dataUltimaQuantitatRebudaAbansCreacio(of.dataUltimaQuantitatRebuda().or(of::dataUltimaQuantitatRebudaAbansCreacio))
                .quantitatRebudaEntradesAcumulatTotal(of.quantitatRebudaEntradesAcumulatTotal() + of.quantitatRebudaEntrades())
                .build();
    }
}