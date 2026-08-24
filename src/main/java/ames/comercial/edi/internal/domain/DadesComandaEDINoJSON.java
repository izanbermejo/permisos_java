package ames.comercial.edi.internal.domain;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = DadesComandaEDINoJSONImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesComandaEDINoJSON {
	Optional<Long> codi ();
	Optional<String> pathPDF();
	String pathEDI();
	String numeroEnviament ();
	String missatgeTipus();
	String missatgeNumero();
	LocalDateTime data();
	String referencia();
	String document();
	Optional<String> observacions();
	String usuariLogistica();
	String codiClientAmes();
	String nomClientAmes();
//	String codiArticle();
//	String codiArticleAmes();
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
	Optional<ObtenirClientEDIAds.ClientEDIAds> clientProfile();
	Optional<String> article();
	String bustia();
}
