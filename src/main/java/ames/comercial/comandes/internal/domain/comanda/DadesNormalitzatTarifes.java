package ames.comercial.comandes.internal.domain.comanda;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = DadesNormalitzatTarifesImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesNormalitzatTarifes {

	Optional<String> tarifaCoixinets();
	Optional<String> tarifaBarres();
	Optional<String> tarifaMedical();
	Optional<String> tarifaIbinsa();
	Optional<String> tarifaFiltresBxx();
	Optional<String> tarifaFiltresSsu();
	Optional<String> tarifaFiltresSxx();
	Optional<String> tarifaFiltresSsuPlaques();

}
