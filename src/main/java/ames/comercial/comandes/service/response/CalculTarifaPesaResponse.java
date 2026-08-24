package ames.comercial.comandes.service.response;

import ames.comercial.comandes.service.CalculadoraUtils;
import ames.comercial.shared.Divisa;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = CalculTarifaPesaResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculTarifaPesaResponse {

	Optional<String> tarifaCoixinets();
	Optional<String> tarifaBarres();
	Optional<String> tarifaIbinsa();
	Optional<String> tarifaMedical();
	Optional<String> tarifaFiltresBxx();
	Optional<String> tarifaFiltresSsu();
	Optional<String> tarifaFiltresSxx();
	Optional<String> tarifaFiltresSsuPlaques();
	List<LiniaNormalitzatResp> linies();
	Optional<BigDecimal> costTransport();
	Optional<BigDecimal> costTransportAfegit();
	Optional<BigDecimal> pesTotal();
	Optional<BigDecimal> importTotalNet();
	Optional<BigDecimal> importTotalBrut();

	static CalculTarifaPesaResponse empty(){
		return CalculTarifaPesaResponseImpl.builder()
				.linies(List.of())
				.build();
	}

	@Derived
	default BigDecimal pes() {
		return CalculadoraUtils.pesKg(linies());
	}

	@Derived
	default BigDecimal importNet() {
		return LiniaNormalitzatResp.importNet(linies());
	}

	@Derived
	default BigDecimal importBrut() {
		return LiniaNormalitzatResp.importBrut(linies());
	}

	@Derived
	default Optional<Divisa> divisa() {
		if (linies().isEmpty())
			return Optional.empty();
		// En cas que hagin línies es retorna la divisa base de la primera línia
		return Optional.of(linies().get(0).preu().divisa().base());
	}

}
