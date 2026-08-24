package ames.comercial.edi.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = DadesLiniaEDIImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesLiniaEDI {

	static String ENUM_STATUS_DRAFT = "DRAFT";
	static String ENUM_STATUS_WARNING = "WARNING";
	static String ENUM_STATUS_ERROR = "ERROR";
	static String ENUM_STATUS_PROCESSED = "PROCESSED";
//	static String ENUM_STATUS_PREFIX = "edi.linies.tipus.";

	Optional<Long> codi();
	Long codi_comanda();
	Optional<Date> dataInicial();
	Optional<Date> dataFinal();
//	Optional<Date> dataAMES();
//	Optional<Date> dataMagatzem();
	int quantitat ();
	Optional<Integer> quantitatActual ();
	Optional<Integer> quantitatAcumulada();
	String codiArticle ();
	String codiArticleAmes ();
	String codiArticleFab ();
	String tipus();
	String observacions();
	Optional<String> comentarisAMES();
	Optional<String> alertesAMES();
	String status();
	Optional<String> ultimAlbara();
	LocalDateTime insertedAt();
	Optional<LocalDateTime> updatedAt();
	Optional<LocalDateTime> deletedAt();
	String insertedBy();
	Optional<String> updatedBy();
	Optional<String> deletedBy();
	Optional<String> updatedReason();
	Optional<String> deletedReason();
	Optional<String> codiLiniaClient();
	Optional<String> codiComandaClient();
	Integer acumulatArticle();
	Optional<String> programa();
}
