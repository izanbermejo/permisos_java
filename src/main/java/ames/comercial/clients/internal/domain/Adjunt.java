package ames.comercial.clients.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.internal.$processor$.meta.$ValueMirrors.Default;

import java.time.LocalDateTime;

@JsonDeserialize(builder = AdjuntImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Adjunt {

	String codiFitxer ();
	String client();
	String nom ();
	Categoria categoria();
	String usuari();
	LocalDateTime data();

	@Default
	default Adjunt canviarNom (String nouNom) {
		return AdjuntImpl.builder()
				.from(this)
				.nom(nouNom)
				.build();
	}
	
}
