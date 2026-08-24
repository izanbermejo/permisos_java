package ames.comercial.comandes.response;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ItemLiniaComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ItemLiniaComanda {

	long codi();
	long numero();
	String empresa();
	TipusLiniaComanda tipus();
	String comandaClient();
	String programa();
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Optional<LocalDate> dataPrevistaSortidaInterna();
	Optional<LocalDate> dataConfirmadaFabrica();
	Preu preu();
	boolean isPreuFixat();
	long quantitat();
	long quantitatServida();
	long quantitatPendent();
	long quantitatAcumulada();
	Optional<String> comentarisInterns();
	Optional<String> comentarisClient();
	Optional<Long> comandaBlanca();
	long numAdjunts();
	String versio();

	/**
	 * Albarans que han servit aquesta línia de comanda. Es retornen ja dins de la query
	 * principal per no haver de fer una crida per línia (permet desplegar-los tots alhora).
	 * Buit si la línia no té res servit.
	 */
	List<AlbaraServitLiniaComanda> albarans();

	@Derived
	default int setmana() {
		return dataSolicitada().get(WeekFields.ISO.weekOfWeekBasedYear());
	}
	
	@Derived
	default String codiNumeroFormat() {
		return String.format("%07d", codi()) + " / " + String.format("%04d", numero()); 
	}

	@Derived
	default boolean isTeAdjunts() {
		return numAdjunts() > 0;
	}

	@Derived
	default boolean servida() {
		return quantitatPendent() == 0;
	}

	@Derived
	default boolean isStockSeguretat() {
		return tipus().isStockSeguretat();
	}
	
}
