package ames.comercial.edi2.request;

import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans.ObtenirUltimsAlbaransResponse;
import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.entrades.internal.domain.InformacioSortidaEdi;
import ames.comercial.shared.Empresa;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(builder = MergeComandaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface MergeComandaRequest {

    List<ObtenirLiniesComandaPendentsResponse> liniesComandaActual();
    InformacioSortidaEdi infoSortida();
    LocalDate dataLimitDiesTall();
    Optional<ObtenirUltimsAlbaransResponse> ultimsAlbarans();
    boolean isConsiderarUltimsAlbarans();
    boolean considerarDuesDates();
    boolean isDuesDates();
    ComandaEdi comanda();

    @Value.Derived
    default List<LiniaEdi> liniesEdi(){
        return comanda().linies();
    }

    @Value.Derived
    default String numComanda(){
        return comanda().numeroComanda();
    }

    @Value.Derived
    default Optional<Long> numPecesEndarrerides(){
        return comanda().numeroPecesEndarrerides();
    }

    @Value.Derived
    default String albaraReferenciaClient(){
        return comanda().referenciaAlbara();
    }

    @Value.Derived
    default boolean isConsiderarAcumulats(){
        return comanda().isConsiderarAcumulats();
    }

    @Value.Derived
    default Optional<Empresa> empresa(){
        return liniesComandaActual().stream()
                .map(ObtenirLiniesComandaPendentsResponse::empresa)
                .findFirst();
    }

    @Value.Derived
    default LocalDate primeraDataEdi(boolean isDuesDates) {
        return liniesEdi().stream()
                .map(l -> l.dataSortidaAMES(isDuesDates))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
    }

    @Value.Derived
    default Optional<String> numeroComandaEDI(){
        return liniesEdi().get(0).numeroComanda();
    }

    @Value.Derived
    default long quantitatEnTransit() {
        if (!isConsiderarUltimsAlbarans())
            return 0;
        return ultimsAlbarans().map(ObtenirUltimsAlbaransResponse::quantitatEnTransit).orElse(0L);
    }
}
