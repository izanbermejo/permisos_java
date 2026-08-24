package ames.comercial.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = AdresaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Adresa {

	String destinatari();
	String adresa();
	String poblacio();
	String codiPostal();
	String pais();

}
