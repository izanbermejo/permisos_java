package ames.comercial.comandes.internal.domain.comanda;

import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

import java.util.Optional;

@JsonDeserialize(builder = ComandaPropsImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ComandaProps {

	TipusComanda tipus();
	DadesComanda dades();
	InformacioClient informacioClient();
	Adresa adresa();
	InformacioEnviament informacioEnviament();
	Optional<DadesNormalitzat> dadesNormalitzat();
	Optional<DadesEnviamentJustificant> dadesEnviamentJustificant();
	@Default default boolean stockSeguretat() { return false; }
	@Default default int numAdjunts() { return 0; }
	Servible servible();
	boolean servida();
	String usuari();
	
}
