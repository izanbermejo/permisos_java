package ames.comercial.comandes.internal.domain.linia;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@JsonDeserialize(builder = DadesCalculNormalitzatImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesCalculNormalitzat {

	BigDecimal descompte();
	long quantitatCalcul();

	default DadesCalculNormalitzat novaQuantitatCalcul(long novaQuantitatCalcul) {
		return DadesCalculNormalitzatImpl.builder()
				.from(this)
				.quantitatCalcul(novaQuantitatCalcul)
				.build();
	}

}
