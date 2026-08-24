package ames.comercial.comandes.request;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.shared.TipusFormatDecimal;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = EnviarJustificantRecepecioEspecialRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface EnviarJustificantRecepecioEspecialRequest {

	String to();
	String cc();
	String assumpte();
	String missatge();
	String idioma();
	Optional<TipusLiniaComanda> tipus();
	TipusFormatDecimal formatNumeric();

}
