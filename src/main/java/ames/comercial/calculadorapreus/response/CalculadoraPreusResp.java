package ames.comercial.calculadorapreus.response;

import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static ames.comercial.shared.Numbers.decimal;
import static ames.comercial.shared.Numbers.descompteAplicar;

@JsonDeserialize(builder = CalculadoraPreusRespImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculadoraPreusResp {

	List<LiniaCalculPreuResp> linies();
	Divisa divisa();

	@Derived
	default BigDecimal impTotal() {
		return linies().stream().map(LiniaCalculPreuResp::imp).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
	}

	default LiniaCalculPreuResp linia(long linia) {
		return linies().stream()
				.filter(l -> l.linia() == linia)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("ID línia no calculada"));
	}

	/**
	 * Representació de les línies calculades
	 */
	@JsonDeserialize(builder = LiniaCalculPreuRespImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	interface LiniaCalculPreuResp {

		long linia();
		KeyArticleClient articleClient();
		long quantitat();
		Preu preu();
		BigDecimal descompte();
		long quantitatCalcul();

		@Derived
		default BigDecimal imp() {
			return preu().imp(quantitat())
					.multiply(descompteAplicar(descompte()))
					.divide(decimal(100), 3, RoundingMode.HALF_UP);
		}

	}

}