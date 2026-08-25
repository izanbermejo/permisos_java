package ames.permisos.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = KeyArticleAmesImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyArticleAmes {

	String codiArticle();
	String codiArticleAmes();
	
	static KeyArticleAmes of(String codi_article, String codi_article_ames) {
		return KeyArticleAmesImpl.builder()
				.codiArticle(codi_article)
				.codiArticleAmes(codi_article_ames)
				.build();
	}

}
