package ames.comercial.comandes.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Albarà que ha servit una línia de comanda. Substitueix la consulta a Advantage
 * ({@code ads/albaransfactures}) obtenint la informació del nou sistema
 * ({@code inventari.moviment} + {@code albarans.albara} + {@code albarans.facturacio}).
 * Els camps repliquen exactament els que retornava
 * {@code QueryAlbaransFacturesByComandesResponse}.
 */
@JsonDeserialize(builder = AlbaraServitLiniaComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface AlbaraServitLiniaComanda {

	String albara();
	Optional<String> albaraEspecial();
	Optional<LocalDate> dataAlbara();
	String enviamentAlbara();
	long quantitat();
	Optional<String> factura();
	boolean entregat();

}
