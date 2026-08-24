package ames.comercial.edi2.internal.domain;

import ames.comercial.entrades.internal.domain.InformacioSortidaEdi;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ConfiguracioEntradaComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ConfiguracioEntradaComanda {

    String codiClient();
    String tipusMissatge();
    String fermOrientatiu();
    String ediBox();
    String nad02();
    String codiProveidor();
    InformacioSortidaEdi informacioSortida();
    boolean considerarAlbarans();
    boolean considerarDuesDates();
    int estrategiaEdi();
    int diesTall();
    List<String> llocsEntrega();
    Optional<String> tipusDocumentEdi();
    Optional<String> comentaris();
    boolean isActiu();

    default boolean isAcceptaLlocEntrega(String lugarEntrega) {
        return llocsEntrega().contains(lugarEntrega);
    }

    @Value.Derived
    default LocalDate dataLimitDiesTall(){
        return LocalDate.now().plusDays(diesTall());
    }

    @Value.Derived
    default List<Integer> diesSortida(){
        return informacioSortida().diesSortida();
    }

    @Value.Derived
    default int diesRestar(){
        return informacioSortida().diesRestar();
    }
}
