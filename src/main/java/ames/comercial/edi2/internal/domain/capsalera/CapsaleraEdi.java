package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = CapsaleraEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CapsaleraEdi {

    long idMissatge();
    long idCapsalera();
    CA ca();
    Optional<CB> cb();
    Optional<CC> cc();
    Optional<CD> cd();
    CI ci();
    Optional<CP> cp();
    Optional<CQ> cq();
    Optional<CF> cf();
    List<CT> ct();

    @Value.Derived
    default String edibox() {
        return ca().buzonOrigen();
    }

    @Value.Derived
    default String codiProveidor() {
        return ci().idProveedor();
    }

    @Value.Derived
    default String tipoMissatge() {
        return ca().documento();
    }

}
