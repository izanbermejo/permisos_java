package ames.comercial.comandes.response;

import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.Optional;

@JsonDeserialize(builder = InformacioEnviamentResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioEnviamentResponse {
    FormaEnviament formaEnviament();
    Incoterm incoterm();
    String desti();
    Optional<String> transportista();
    Optional<String> zonaTransport();

    @Derived
    default String informacioEnviamentStr() {
        return formaEnviament().codiAdvantage() + incoterm().toString() + desti();
    }

    static InformacioEnviamentResponse of(InformacioEnviament info) {
        return InformacioEnviamentResponseImpl.builder()
                .formaEnviament(info.formaEnviament())
                .incoterm(info.incoterm())
                .desti(info.desti())
                .transportista(info.transportista())
                .zonaTransport(info.zonaTransport())
                .build();
    }

}
