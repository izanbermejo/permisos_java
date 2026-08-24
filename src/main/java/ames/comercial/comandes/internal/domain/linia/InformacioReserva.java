package ames.comercial.comandes.internal.domain.linia;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = InformacioReservaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioReserva {

	Reservable estat();
	long quantitat();

	static InformacioReserva empty() {
		return InformacioReservaImpl.builder()
				.estat(Reservable.NO_APLICA)
				.quantitat(0)
				.build();
	}

	static InformacioReserva of(long quantitatPendentServir, long quantitatReservable) {
		var qtatReservar = Math.min(quantitatPendentServir, quantitatReservable);
		var reservable = Reservable.RES;
		if (qtatReservar > 0) {
			reservable = qtatReservar < quantitatPendentServir ? Reservable.PARCIAL : Reservable.TOT;
		}
		return InformacioReservaImpl.builder()
				.estat(reservable)
				.quantitat(qtatReservar)
				.build();
	}

}
