package ames.comercial.edi.request;

import ames.comercial.server.I18N;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;

import java.time.LocalDate;

@JsonDeserialize(builder = DadesLiniaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesLiniaRequest {

	int quantitat();
	LocalDate dataClient();
	LocalDate dataAMES();
	LocalDate dataMagatzem();

	@Check
	default void check () {
		Preconditions.checkArgument(quantitat() >= 2, I18N.getLiteral("quantitat_major_zero"));
	}

}
