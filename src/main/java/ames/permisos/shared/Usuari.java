package ames.permisos.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = UsuariImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Usuari {

	String nom();
	String id();
	
	static Usuari of(String nom, String id) {
		return UsuariImpl.builder()
				.nom(nom)
				.id(id)
				.build();
	}

}
