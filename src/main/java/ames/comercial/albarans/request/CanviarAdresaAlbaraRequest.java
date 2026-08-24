package ames.comercial.albarans.request;

import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CanviarAdresaAlbaraRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CanviarAdresaAlbaraRequest {

	String destinatari();
	String adresa();
	String poblacio();
	String codiPostal();
	String transportista();
	String pais();
	Optional<String> zonaTransport();
	FormaEnviament formaEnviament();
	Incoterm incoterm();
	Optional<String> desti();

}
