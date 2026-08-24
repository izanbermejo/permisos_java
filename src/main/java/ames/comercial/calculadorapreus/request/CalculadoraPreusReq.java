package ames.comercial.calculadorapreus.request;

import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = CalculadoraPreusReqImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculadoraPreusReq {

	String client();
	Divisa divisa();
	Optional<String> tarifaCoixinets();
	Optional<String> tarifaBarres();
	Optional<String> tarifaIbinsa();
	Optional<String> tarifaMedical();
	Optional<String> tarifaFiltresBxx();
	Optional<String> tarifaFiltresSsu();
	Optional<String> tarifaFiltresSxx();
	Optional<String> tarifaFiltresSsuPlaques();
	List<LiniaCalculPreuReq> linies();

	@JsonDeserialize(builder = LiniaCalculPreuReqImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	interface LiniaCalculPreuReq {
		long linia();
		KeyArticleClient articleClient();
		long quantitat();

		static LiniaCalculPreuReq of (long linia, KeyArticleClient articleClient, long quantitat) {
			return LiniaCalculPreuReqImpl.builder()
					.linia(linia)
					.articleClient(articleClient)
					.quantitat(quantitat)
					.build();
		}
	}
	
}