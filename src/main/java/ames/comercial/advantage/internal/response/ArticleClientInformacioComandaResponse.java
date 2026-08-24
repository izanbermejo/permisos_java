package ames.comercial.advantage.internal.response;

import ames.comercial.inventari.ext.IObtenirFitxesMagatzem.FitxaMagatzemResponse;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem.FitxaMagatzemSatelitResponse;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ArticleClientInformacioComandaResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ArticleClientInformacioComandaResponse {

	String artint();
	String aclfab();
	String codiEmpresa();
	String descEmpresa();
	String codiEmpresaEntrega();
	String descEmpresaEntrega();
	String codiEmpresaClient();
	String denominacio();
	String referencia();
	String nivellTecnic();
	long unitatsEmbalatge();
	long numCaixesPalet();
	String codiClient();
	String nomClient();
	String codiFabrica();
	String descFabrica();
	String diesSortida();
	long diesTransitClient();
	FormaEnviament formaEnviament();
	Incoterm incoterm();
	String desti();
	String codiTransportista();
	BigDecimal preu();
	String divisa();
	String magatzemEntrada();
	String magatzemEntradaDesc();
	String magatzemSortida();
	String magatzemSortidaDesc();
	boolean matazemSortidaPlataforma();
	@Default default long diesTransitEntreMagatzems() { return 0L; }
	String notesMorositat();
	String notesClient();
	String notesLogistica();
	String notesEmbalatge();
	boolean isClientEDI();
	boolean isImpagament();
	boolean isClientProforma();
    String comentariInternClient();
    String comentariInternArticle();
	Optional<LocalDate> dataUltimaComanda();

	@Default default List<FitxaMagatzemResponse> stocksMagatzems() {return List.of(); };
	@Default default List<FitxaMagatzemSatelitResponse> stocksEstantsSatelit() { return List.of(); };

	@Derived default boolean isFaMesUnAnyDataUltimaComanda() {
		return dataUltimaComanda()
				.map(data -> data.isBefore(LocalDate.now().minusYears(1)))
				.orElse(false);
	}
	
	@Derived default boolean necessitaMagatzemIntermig() {
		return !magatzemEntrada().equals(magatzemSortida());
	}

	@Derived default KeyArticleClient keyArticleClient() {
		return KeyArticleClient.of(artint(), codiClient());
	}

	@Derived default Long stockTotal () {
		return stocksMagatzems().stream().mapToLong(FitxaMagatzemResponse::stock).sum();
	}

	@Derived default Long stockSatelit () {
		return stocksEstantsSatelit().stream().mapToLong(FitxaMagatzemSatelitResponse::stock).sum();
	}

	@Derived default boolean isTePesesSatelit () {
		return stockSatelit() > 0;
	}

	@Derived default boolean isTeStocksNegatius () {
		return stocksMagatzems().stream().anyMatch(s -> s.stock() < 0);
	}

}
