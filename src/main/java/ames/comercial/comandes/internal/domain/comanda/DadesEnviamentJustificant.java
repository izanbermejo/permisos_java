package ames.comercial.comandes.internal.domain.comanda;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.List;

@JsonDeserialize(builder = DadesEnviamentJustificantImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesEnviamentJustificant {

	String to();
	String cc();
	String assumpte();
	String missatge();
	List<String> adjunts();
	LocalDateTime data();
	String usuari();

}
