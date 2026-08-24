package ames.comercial.comandes.response;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;

@JsonDeserialize(builder = ObtenirLiniesComandaResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ObtenirLiniesComandaResponse {

	ArticleClientInformacioComandaResponse info();
	List<ItemLiniaComanda> linies();
	
}
