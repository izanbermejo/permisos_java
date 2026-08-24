package ames.comercial.edi.request;

import ames.comercial.edi.response.ComandaEDIResponse;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniaArticleEditRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaArticleEditRequest {

	ComandaEDIResponse comanda();
	LocalDate dataClient();
	LocalDate dataAmes();
	Optional<LocalDate> dataMagatzem();
	Integer quantitat();
	String tipus();
	Optional<String> nad();
	Optional<String> codiComandaClient();
	Optional<String> codiComandaClientProcessat();
//	Optional<Boolean> processable();
}
