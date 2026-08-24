package ames.comercial.comandes.internal.infraestructure.comanda.mapper;

import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ComandaRecord {

	long codi();
	TipusComanda tipus();
	DadesComanda dades();
	InformacioClient informacioClient();
	Adresa adresa();
	InformacioEnviament informacioEnviament();
	Optional<DadesNormalitzat> dadesNormalitzat();
	Optional<DadesEnviamentJustificant> dadesEnviamentJustificant();
	boolean stockSeguretat();
	int numAdjunts();
	boolean servida();
	Servible servible();
	String usuari();

}
