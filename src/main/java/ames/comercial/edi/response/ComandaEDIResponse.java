package ames.comercial.edi.response;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesLiniaEDIImpl;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ComandaEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ComandaEDIResponse {

	DadesComandaEDINoJSON comanda();
	Optional<String> codiClient();
	Optional<String> nomClientAmes();
	Optional<String> codiArticle();
	Optional<String> codiArticleAmes();
	Optional<String> codiArticleFab();
	Optional<String> missatgeNumero();
	Optional<String> perfilDiesSortida();
	Optional<Integer> perfilDiesResta();
	Optional<String> ultimAlbara();
	Optional<List<LiniaEDIResponse>> linies();
	Optional<String> log();
	Optional<String> pathPDF();
//	Long codiComanda();
	@Value.Default
	default Boolean processable() {
		return true;
	}
	Optional<Integer> acumulatArticle();
	Optional<Date> dataAcumulat();
	Optional<String> albaraAcumulat();
	Optional<String> stockAcumulat();
	@Value.Default
	default Boolean coincideixenAcumulats() {
		return true;
	}
	@Value.Default
	default Boolean ultimAlbaraRebutTrobat() {
		return true;
	}
	Long stockArticle();
}
