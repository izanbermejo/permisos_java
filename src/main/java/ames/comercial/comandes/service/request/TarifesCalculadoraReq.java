package ames.comercial.comandes.service.request;

import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzatTarifes;
import ames.comercial.comandes.service.IProviderTarifaActual.ProviderTarifaActualResponse;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = TarifesCalculadoraReqImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface TarifesCalculadoraReq {

	Optional<String> coixinets();
	Optional<String> barres();
	Optional<String> ibinsa();
	Optional<String> medical();
	Optional<String> filtresBxx();
	Optional<String> filtresSsu();
	Optional<String> filtresSxx();
	Optional<String> filtresSsuPlaques();

	static TarifesCalculadoraReq from (ProviderTarifaActualResponse req) {
		return TarifesCalculadoraReqImpl.builder()
				.coixinets(req.coixinets())
				.barres(req.barres())
				.ibinsa(req.ibinsa())
				.medical(req.medical())
				.filtresBxx(req.filtresBxx())
				.filtresSsu(req.filtresSsu())
				.filtresSxx(req.filtresSxx())
				.filtresSsuPlaques(req.filtresSsuPlaques())
				.build();
	}

	static TarifesCalculadoraReq from (DadesNormalitzatTarifes req) {
		return TarifesCalculadoraReqImpl.builder()
				.coixinets(req.tarifaCoixinets())
				.barres(req.tarifaBarres())
				.ibinsa(req.tarifaIbinsa())
				.medical(req.tarifaMedical())
				.filtresBxx(req.tarifaFiltresBxx())
				.filtresSsu(req.tarifaFiltresSsu())
				.filtresSxx(req.tarifaFiltresSxx())
				.filtresSsuPlaques(req.tarifaFiltresSsuPlaques())
				.build();
	}

}