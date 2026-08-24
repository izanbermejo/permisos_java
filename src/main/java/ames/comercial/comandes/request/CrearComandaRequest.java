package ames.comercial.comandes.request;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = CrearComandaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CrearComandaRequest {

	String articleClient();
	// Dades de la capçalera de la comanda
	String comanda();
	String programa();
	// Dades de la 1a línia
	long quantitat();
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Optional<LocalDate> dataPrevistaSortidaInterna();
	Optional<LocalDate> dataConfirmadaFabrica();
	TipusLiniaComanda tipus();
	BigDecimal preu();
	String divisa();
	boolean isPreuFixat();
	Optional<Long> comandaBlanca();

}
