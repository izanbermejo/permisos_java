package ames.comercial.comandes.service.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(builder = MissatgesCanvisComandaResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface MissatgesCanvisComandaResponse {

	List<String> missatgesPreus();
	List<String> missatgesPreusServides();

	static MissatgesCanvisComandaResponse of (List<String> missatgesPreus, List<String> missatgesPreusServides) {
		return MissatgesCanvisComandaResponseImpl.builder()
				.missatgesPreus(missatgesPreus)
				.missatgesPreusServides(missatgesPreusServides)
				.build();
	}

}
