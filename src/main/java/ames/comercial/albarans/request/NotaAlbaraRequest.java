package ames.comercial.albarans.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = NotaAlbaraRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface NotaAlbaraRequest {

	String artint();
	String clicod();
	String nota();

}
