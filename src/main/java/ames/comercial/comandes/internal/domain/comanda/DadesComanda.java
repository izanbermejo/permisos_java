package ames.comercial.comandes.internal.domain.comanda;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = DadesComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesComanda {

	String client ();
	String clientNom();
	String empresa ();
	LocalDate dataAlta ();
	
}
