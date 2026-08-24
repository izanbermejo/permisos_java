package ames.comercial.comandes.service;

import ames.comercial.comandes.service.ProviderPreu.IProviderPreuResponse;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface IProviderPreu {

	IProviderPreuResponse provide(ProviderPreuRequest request);

	@JsonDeserialize(builder = ProviderPreuRequestImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	interface ProviderPreuRequest {
		Set<KeyArticleClient> articles();
		Optional<String> tarifaCoixinets();
		Optional<String> tarifaBarres();
		Optional<String> tarifaIbinsa();
		Optional<String> tarifaMedical();
		Optional<String> tarifaFiltresBxx();
		Optional<String> tarifaFiltresSsu();
		Optional<String> tarifaFiltresSxx();
		Optional<String> tarifaFiltresSsuPlaques();
		@Default default BigDecimal factorAplicar() { return BigDecimal.ONE; }
	}

}
