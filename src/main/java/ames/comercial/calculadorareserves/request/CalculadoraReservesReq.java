package ames.comercial.calculadorareserves.request;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = CalculadoraReservesReqImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculadoraReservesReq {

	long stock();
	List<LiniaCalculReservesReq> linies();
	Optional<LiniaCalculReservesReq> liniaPreferent();

	@JsonDeserialize(builder = LiniaCalculReservesReqImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	interface LiniaCalculReservesReq {
		KeyLiniaComanda clauLinia();
		long quantitat();
		LocalDateTime dataSolicitudReserva();

		static LiniaCalculReservesReq of (KeyLiniaComanda clauLinia, long quantitat, LocalDateTime dataSolicitudReserva) {
			return LiniaCalculReservesReqImpl.builder()
					.clauLinia(clauLinia)
					.quantitat(quantitat)
					.dataSolicitudReserva(dataSolicitudReserva)
					.build();
		}
	}
	
}