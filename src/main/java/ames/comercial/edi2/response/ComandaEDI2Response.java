package ames.comercial.edi2.response;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = ComandaEDI2ResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ComandaEDI2Response {

    Optional<KeyLiniaComanda> clauLinia();
    Optional<Long> idLinia();
    Optional<String> comandaClient();
    TipusLiniaComanda tipusLiniaComanda();
    long quantitatNova();
    long quantitatActual();
    LocalDate dataSolicitada();
    LocalDate dataSortida();
    Optional<LocalDate> dataPrevistaSortidaInterna();
    Optional<LocalDate> dataConfirmada();
    long acumulatNova();
    long acumulatActual();
    String observacions();
    boolean isNeutre();
    Optional<String> comentarisInterns();
    Optional<String> comentarisClient();
    Optional<Preu> infoArticle();
    Optional<Long> comandaBlanca();
    Optional<String> referencia();
    Optional<Empresa> empresa();
    Optional<String> clientNom();
    Optional<Boolean> isPreuFixat();

    @Value.Derived
    default String comandaClientActual() {
        return clauLinia().isPresent() ? comandaClient().orElse("") : "";
    }

    //Deriveds necessaris per al modal edicio de la linia per a comandes internes.
    @Value.Derived
    default long quantitatAcumulada(){return acumulatActual();}

    @Value.Derived
    default TipusLiniaComanda tipus(){return tipusLiniaComanda();}

    @Value.Derived
    default Optional<Long> codi(){return clauLinia().map(KeyLiniaComanda::comanda);}

    @Value.Derived
    default Optional<String> codiNumeroFormat() {
        return clauLinia().flatMap(clau -> {
            if (clau.comandaFormat() == null && clau.numeroFormat() == null) {
                return Optional.empty();
            }

            return Optional.of(
                    (clau.comandaFormat() != null ? clau.comandaFormat() : "") +
                            " / " +
                            (clau.numeroFormat() != null ? clau.numeroFormat() : "")
            );
        });
    }

    @Value.Derived
    default Optional<Long> numero(){return clauLinia().map(KeyLiniaComanda::numero);}

    @Value.Derived
    default long quantitat(){return quantitatActual();}

    @Value.Derived
    default LocalDate dataPrevistaSortida(){return dataSortida();}

    @Value.Derived
    default Optional<Preu> preu(){return infoArticle();}

    @Value.Derived
    default Optional<String> codiEmpresaClient(){return empresa().map(Empresa::clau);}
}
