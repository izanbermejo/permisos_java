package ames.comercial.comandes.request;

import ames.comercial.shared.TipusFormatDecimal;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = EnviarJustificantRecepecioRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface EnviarJustificantRecepecioRequest {

	String to();
	String cc();
	String assumpte();
	String missatge();
	String idioma();
	boolean isFormatDistribuidor();
	TipusFormatDecimal formatNumeric();

}
