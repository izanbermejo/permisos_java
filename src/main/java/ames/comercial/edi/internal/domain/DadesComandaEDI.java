package ames.comercial.edi.internal.domain;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.beans.ComandaMissatgeEDI;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = DadesComandaEDIImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesComandaEDI {

	static String ENUM_STATUS_DRAFT = "DRAFT";
	static String ENUM_STATUS_WARNING = "WARNING";
	static String ENUM_STATUS_ERROR_NAD_NOTFOUND = "ERROR_NAD_NOTFOUND";
	static String ENUM_STATUS_ERROR_REFERENCIA_ARTICLE_NOTFOUND = "ERROR_REFERENCIA_ARTICLE_NOTFOUND";
	static String ENUM_STATUS_ERROR_REFERENCIA_ARTICLE_NOTFOUND_EMPTY = "ERROR_REFERENCIA_ARTICLE_NOTFOUND_EMPTY";
	static String ENUM_STATUS_ERROR_NAD_DUPLICATED = "ERROR_NAD_DUPLICATED";
	static String ENUM_STATUS_ERROR_CLIENT_SENSE_FLAGS = "ERROR_CLIENT_SENSE_FLAGS";
	static String ENUM_STATUS_ERROR_CODI_COMANDA_NO_INFORMAT = "ERROR_CODI_COMANDA_NO_INFORMADA";
	static String ENUM_STATUS_PROCESSED = "PROCESSED";
	static String ENUM_STATUS_NOT_PROCESSED = "NOT_PROCESSED";

	Optional<Long> codi ();
	ComandaMissatgeEDI json ();
    String pathEDI();
	String document();
	Optional<String> pathPDF();
	String numeroEnviament ();
	String missatgeTipus();
	String missatgeNumero();
	LocalDateTime data();
	String referencia();
	Optional<String> observacions();
	String usuariLogistica();
	String codiClientAmes();
	String nomClientAmes();
	Boolean deleted();
	LocalDateTime insertedAt();
	Optional<LocalDateTime> updatedAt();
	Optional<LocalDateTime> deletedAt();
	String insertedBy();
	Optional<String> updatedBy();
	Optional<String> deletedBy();
	Optional<String> updatedReason();
	Optional<String> deletedReason();
	String status();
	String nad();
	String nadPath();
	String tipus();
	Optional<String> bustia();
	Optional<ObtenirClientEDIAds.ClientEDIAds> clientProfile();
//	Optional<String> codiComandaClient();
}
