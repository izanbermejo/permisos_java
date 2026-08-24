package ames.comercial.comandes.internal.domain.comanda;

import ames.comercial.shared.Divisa;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@JsonDeserialize(builder = DadesNormalitzatImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesNormalitzat {

	BigDecimal importNet();
	BigDecimal importBrut();
	Divisa divisa();
	BigDecimal pes();
	BigDecimal costTransport();
	DadesNormalitzatTarifes tarifes();

	default DadesNormalitzat actualitzarCostTransport(BigDecimal costTransport) {
		return DadesNormalitzatImpl.builder().from(this)
				.costTransport(costTransport)
				.build();
	}
	
}
