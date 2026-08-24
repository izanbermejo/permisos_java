package ames.comercial.comandes.service.response;

import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

import static ames.comercial.shared.Numbers.decimal;
import static ames.comercial.shared.Numbers.descompteAplicar;

@JsonDeserialize(builder = LiniaNormalitzatRespImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaNormalitzatResp {

	long linia();
	KeyArticleClient articleClient();
	String referencia();
	TipusArticleClient tipusArticleClient();
	long quantitat();
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Preu preu();
	boolean isPreuFixat();
	BigDecimal descompte();
	BigDecimal pes();
	long quantitatCalcul();
	Reservable reservable();
	long quantitatReservable();
	long stockDisponible();

	@Derived
	default boolean servir() {
		return quantitatReservable() == quantitat();
	}
	
	@Derived
	default long quantitatPendentReservar() {
		if (!servir())
			return quantitat() - quantitatReservable();
		return 0;
	}
	
	@Derived
	default BigDecimal importNet() {
		return preu().imp(quantitat())
				.multiply(descompteAplicar(descompte()))
				.divide(decimal(100), 3, RoundingMode.HALF_UP);
	}

	@Derived
	default BigDecimal preuNet() {
		return preu().valor()
				.multiply(descompteAplicar(descompte()))
				.divide(decimal(100), 3, RoundingMode.HALF_UP);
	}

	@Derived
	default BigDecimal pesKg() {
		return pes().divide(decimal(1000), 2, RoundingMode.HALF_UP);
	}

	@Derived
	default BigDecimal importBrut() {
		return preu().imp(quantitat());
	}

	@Derived
	default int setmana() {
		return dataSolicitada().get(WeekFields.ISO.weekOfWeekBasedYear());
	}

	static BigDecimal importNet(List<LiniaNormalitzatResp> linies) {
		return linies.stream()
				.map(LiniaNormalitzatResp::importNet)
				.reduce(BigDecimal::add)
				.orElseGet(() -> BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP);
	}

	static BigDecimal importBrut(List<LiniaNormalitzatResp> linies) {
		return linies.stream()
				.map(LiniaNormalitzatResp::importBrut)
				.reduce(BigDecimal::add)
				.orElseGet(() -> BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP);
	}
}
