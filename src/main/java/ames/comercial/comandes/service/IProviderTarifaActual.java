package ames.comercial.comandes.service;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

public interface IProviderTarifaActual {

    ProviderTarifaActualResponse executar(String clicod);

    @JsonDeserialize(builder = ProviderTarifaActualResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface ProviderTarifaActualResponse {
        Optional<String> coixinets();
        Optional<String> barres();
        Optional<String> ibinsa();
        Optional<String> medical();
        Optional<String> filtresBxx();
        Optional<String> filtresSsu();
        Optional<String> filtresSxx();
        Optional<String> filtresSsuPlaques();
    }

}
