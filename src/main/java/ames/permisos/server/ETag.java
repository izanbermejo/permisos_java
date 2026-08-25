package ames.permisos.server;

import java.time.LocalDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/** 
 *	Informació sobre l'actualització d'un recurs.
 *	En les peticions de consulta sobre un recurs, es retorna la informació requerida més el codi de versió ETag.
 *	En les peticions de modificació, es verifica que aquest codi sigui el mateix que el del recurs. Com que cada   
 * 	vegada que un recurs és actualitzat es genera un nou ETag amb un número de versió aleatori, si els dos codis
 * 	ETag no coincideixen, voldrà dir aque el recurs ha estat modificat per una altre usuari. 
 */

@JsonDeserialize(builder = ETagImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl") 
@Value.Immutable
public interface ETag { 
	
	LocalDateTime data ();
	String versio ();
	String usuari ();
	
	public static ETag generate () {
		return ETagImpl.builder()
			.versio(UUID.randomUUID().toString())
			.data(LocalDateTime.now().withNano(0))
			.usuari(RequestThread.nomUsuari())
			.build();
	}
	
	public static ETag of(LocalDateTime data, String versio, String usuari) {
		return ETagImpl.builder()
				.versio(versio)
				.data(data)
				.usuari(usuari)
				.build();		
	}
	
}
