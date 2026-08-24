package ames.comercial.comandes.response;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Optional;

@JsonDeserialize(builder = ItemLiniaComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ItemHistoriaLiniaComanda {

	long comanda();
	long numero();
	TipusLiniaComanda tipus();
	long quantitat();
	BigDecimal preu();
	String divisa();
	boolean isPreuFixat();
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Optional<LocalDate> dataPrevistaSortidaInterna();
	Optional<LocalDate> dataConfirmadaFabrica();
	long quantitatServida();
	Optional<Long> comandaBlanca();
	LocalDateTime datareg();
	String usuari();
	
	@Derived
	public default long quantitatPendent() {
		return Math.max(0, quantitat() - quantitatServida());
	}
	
	@Derived
	public default int setmana() {
		return dataSolicitada().get(WeekFields.ISO.weekOfWeekBasedYear());
	}
	
}
