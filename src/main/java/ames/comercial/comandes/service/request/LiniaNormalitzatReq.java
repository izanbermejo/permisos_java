package ames.comercial.comandes.service.request;

import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = LiniaNormalitzatReqImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaNormalitzatReq {

	long linia();
	KeyArticleClient articleClient();
	long quantitat();
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Optional<Preu> preuFixat();
	@Default default boolean isCalcularReserves() { return true; }
	
	@Derived
	default String artcli() {
		return articleClient().artint() + "_" + articleClient().clicod();
	}

	static LiniaNormalitzatReq of (LiniaComanda linia) {
		return of (linia, true);
	}

	static List<LiniaNormalitzatReq> of (List<LiniaComanda> linies) {
		return linies.stream().map(LiniaNormalitzatReq::of).toList();
	}

	static LiniaNormalitzatReq of (LiniaComanda linia, boolean isCalcularReserves) {
		return LiniaNormalitzatReqImpl.builder()
				.linia(linia.numero())
				.articleClient(linia.articleClient())
				.quantitat(linia.quantitat())
				.dataSolicitada(linia.dataSolicitada())
				.dataPrevistaSortida(linia.dataPrevistaSortida())
				.isCalcularReserves(isCalcularReserves)
				.preuFixat(linia.isPreuFixat() ? Optional.of(linia.preu()) : Optional.empty())
				.build();
	}

	static LiniaNormalitzatReq ofNoPreuFixat (LiniaComanda linia, boolean isCalcularReserves) {
		return LiniaNormalitzatReqImpl.builder()
				.linia(linia.numero())
				.articleClient(linia.articleClient())
				.quantitat(linia.quantitat())
				.dataSolicitada(linia.dataSolicitada())
				.dataPrevistaSortida(linia.dataPrevistaSortida())
				.isCalcularReserves(isCalcularReserves)
				.preuFixat(Optional.empty())
				.build();
	}

}