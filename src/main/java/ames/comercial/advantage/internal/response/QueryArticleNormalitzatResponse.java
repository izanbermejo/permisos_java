package ames.comercial.advantage.internal.response;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = QueryArticleNormalitzatResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryArticleNormalitzatResponse {

	String artint();
	String clicod();
	String codi();
	String referencia();
	String denominacio();
	String fabrica();
	long stock();
	long reserva();
	long stockLocal();
	long reservaLocal();
	long unitatsEmbalatge();
	long bossesCaixa();
	long caixesPalet();
	
}
