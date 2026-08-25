package ames.permisos.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.internal.$processor$.meta.$ValueMirrors.Derived;

@JsonDeserialize(builder = KeyArticleClientImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyArticleClient {

	String artint();
	String clicod();
	
	static KeyArticleClient of(String artint, String clicod) {
		return KeyArticleClientImpl.builder()
				.artint(artint)
				.clicod(clicod)
				.build();
	}

	static KeyArticleClient ofNormalitzat(String artint) {
		return KeyArticleClientImpl.builder()
				.artint(artint)
				.clicod("000000")
				.build();
	}

	@Derived
	default boolean isNormalitzat() {
		return "000000".equals(clicod());
	}

	default boolean equals(KeyArticleClient key) {
		if (key==null)
			return false;
		return key.artint().equals(artint()) && key.clicod().equals(clicod());
	}

	default boolean equals(String artint, String clicod) {
		return equals(KeyArticleClient.of(artint, clicod));
	}

}
