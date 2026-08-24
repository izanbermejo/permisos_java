package ames.comercial.advantage.internal.response;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = QueryClientResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryClientResponse {

	String codi();
	String codiEmpresa();
	String descEmpresa();
	String nom();
	String alias();
	String zona();
	String nif();
	String codiProveidor();
	String flag();
	Boolean isImpagament();
	String usulogis();
	String nomPais();

}
