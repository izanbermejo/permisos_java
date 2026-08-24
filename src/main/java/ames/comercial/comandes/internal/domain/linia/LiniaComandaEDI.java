package ames.comercial.comandes.internal.domain.linia;

import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniaComandaEDIImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaComandaEDI {

//	KeyLiniaComanda id();
	KeyArticleClient articleClient();
//	TipusArticleClient tipusArticleClient();
//	String referencia();
	TipusLiniaComanda tipus();
	long quantitat();
//	Preu preu();
//	@Default default boolean isPreuFixat() { return  false; }
	LocalDate dataClient();
	Optional<LocalDate> dataAMES();
//	Optional<LocalDate> dataPrevistaSortidaInterna();
	Optional<LocalDate> dataMagatzem();
//	long quantitatServida();
//	Optional<InformacioReserva> reserva();
//	Optional<DadesCalculNormalitzat> dadesCalcul();
//	Optional<LocalDate> datareg(); // TODO Eliminar després de la migració
	Optional<String> usuari();
//	Optional<DadesXAlbara> dadesAlbara();

	//	@Derived
//	default long comanda() {
//		return id().comanda();
//	}
//
//	@Derived
//	default long numero() {
//		return id().numero();
//	}

//	@Derived
//	default BigDecimal importNet() {
//		return preu().imp(quantitat())
//				.multiply(descompteAplicar(dadesCalcul().map(DadesCalculNormalitzat::descompte).orElse(BigDecimal.ZERO)))
//				.divide(decimal(100), 3, RoundingMode.HALF_UP);
//	}
//
//	@Derived
//	default BigDecimal importBrut() {
//		return preu().imp(quantitat());
//	}

//	@Derived
//	public default long quantitatPendent() {
//		return Math.max(0, quantitat() - quantitatServida());
//	}

//	@Derived
//	default boolean servida() {
//		return quantitatPendent() == 0;
//	}
//
//	@Derived
//	default long quantitatReservada() { return reserva().map(InformacioReserva::quantitat).orElse(0L); }
//
//	/**
//	 * @return Quantitat de peces pendents de reservar de la línia
//	 */
//	@Derived
//	default long quantitatPendentReservar() { return quantitatPendent() - quantitatReservada(); }

	/**
	 * @return True quan la línia té peces pendents de reservar, false altrament
	 */
//	@Derived
//	default boolean teQuantitatPendentReservar() { return quantitatPendentReservar() > 0; }

	default LiniaComandaEDI canviarDates(LocalDate dataClient, LocalDate dataAMES, LocalDate dataMagatzem) {
		return LiniaComandaEDIImpl.builder()
				.from(this)
				.dataClient(dataClient)
				.dataAMES(dataAMES)
				.dataMagatzem(dataMagatzem)
				.build();
	}

	default LiniaComandaEDI canviarQuantitat(long novaQuantitat) {
		return LiniaComandaEDIImpl.builder()
				.from(this)
				.quantitat(novaQuantitat)
				.build();
	}

	/**
	 * @param qtatTreure Qtat de peces a treure de la reserva
	 * @return Actualitza la informació de reserva de la línia després de treure la quantitat de peces
	 * de la reserva
	 */
//	default LiniaComandaEDI treureReserva(long qtatTreure) {
//		var oldReserva = reserva().orElseThrow();
//		var newQtat = oldReserva.quantitat() - qtatTreure;
//		// TODO newQtat < 0 -> Excepció ja que no es pot treure mes reserva de la que hi ha
//		var newReserva = InformacioReserva.of(quantitatPendent(), newQtat);
//		return LiniaComandaImpl.builder()
//				.from(this)
//				.reserva(newReserva)
//				.build();
//	}

//	default LiniaComandaEDI afegirReserva(long qtatAfegir) {
//		var oldReserva = reserva().orElseThrow();
//		var newQtat = oldReserva.quantitat() + qtatAfegir;
//		// TODO newQtat > quantitatPendent -> Excepció ja que no es pot afegir mes reserva de la que hi ha
//		var newReserva = InformacioReserva.of(quantitatPendent(), newQtat);
//		return LiniaComandaImpl.builder()
//				.from(this)
//				.reserva(newReserva)
//				.build();
//	}
	
//	@Derived
//	public default String codiFormat() { return String.format("%07d", id().comanda()); }
//
//	@Derived
//	public default String numeroFormat() {
//		return String.format("%04d", id().numero());
//	}
//
//	@Derived
//	public default String codiNumeroFormat() {
//		return codiFormat() + " / " + numeroFormat();
//	}
	
//	public default LiniaComandaEDI servir (long quantitatServir) {
//		Optional<InformacioReserva> newInfoReserva = Optional.empty();
//		// En cas de normalitzats s'ha de tornar a calcular la informació de la reserva
//		if (TipusArticleClient.NORMALITZAT.equals(tipusArticleClient())) {
//			var infoReserva = reserva().orElseThrow();
//			// La quantitat a servir nor pot superar la quantitat reservada
//			if (quantitatServir > infoReserva.quantitat())
//				throw new AppException("La quantitat a servir supera la quantitat reservada");
//			var novaQuantitatReservada = infoReserva.quantitat() - quantitatServir;
//			var novaQuantitatPendent = quantitatPendent() - quantitatServir;
//			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
//					.quantitat(infoReserva.quantitat() - quantitatServir)
//					.estat(Reservable.of(novaQuantitatReservada, novaQuantitatPendent))
//					.build());
//		}
//		return LiniaComandaImpl.builder().from(this)
//				.quantitatServida(quantitatServida()+quantitatServir)
//				.reserva(newInfoReserva)
//				.build();
//	}
//
//	default LiniaComandaEDI eliminar() {
//		if (quantitatServida() > 0)
//			throw new AppException("No es pot eliminar una línia on ja s'han servit peces");
//		Optional<InformacioReserva> newInfoReserva = Optional.empty();
//		// En cas de normalitzats s'ha de tornar a calcular la informació de la reserva
//		if (TipusArticleClient.NORMALITZAT.equals(tipusArticleClient())) {
//			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
//					.quantitat(0)
//					.estat(Reservable.NO_APLICA)
//					.build());
//		}
//		return LiniaComandaImpl.builder().from(this)
//				.quantitat(0)
//				.reserva(newInfoReserva)
//				.build();
//	}
//
//	default LiniaComandaEDI fixarPreu(Preu preu) {
//		// Dades càlcul a 0
//		var dadesCalcul = DadesCalculNormalitzatImpl.builder()
//				.from(dadesCalcul().orElseThrow())
//				.quantitatCalcul(0)
//				.build();
//		return LiniaComandaImpl.builder()
//				.from(this)
//				.isPreuFixat(true)
//				.preu(preu)
//				.dadesCalcul(dadesCalcul)
//				.build();
//	}
//
//	default LiniaComandaEDI desfixarPreu(Preu preu, BigDecimal descompte, long quanitatCalcul) {
//		var dadesCalcul = DadesCalculNormalitzatImpl.builder()
//				.from(dadesCalcul().orElseThrow())
//				.quantitatCalcul(quanitatCalcul)
//				.descompte(descompte)
//				.build();
//		return LiniaComandaImpl.builder()
//				.from(this)
//				.isPreuFixat(false)
//				.preu(preu)
//				.dadesCalcul(dadesCalcul)
//				.build();
//	}
//
//	default LiniaComandaEDI anular (){
//		Optional<InformacioReserva> newInfoReserva = Optional.empty();
//		// En cas de normalitzats s'ha de tornar a calcular la informació de la reserva
//		if (TipusArticleClient.NORMALITZAT.equals(tipusArticleClient())) {
//			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
//					.quantitat(0)
//					.estat(Reservable.NO_APLICA)
//					.build());
//		}
//		return LiniaComandaImpl.builder().from(this)
//				.quantitat(quantitatServida())
//				.reserva(newInfoReserva)
//				.build();
//	}
//
//	// TODO Eliminar després de la migració
//	@Derived
//	public default String clau() {
//		return id().comanda() + "_" + id().numero();
//	}
//
//	/**
//	 * Càlcula la diferència d'stock reserva entre dos línies de comanda
//	 * @param linA {@link LiniaComandaEDI} Linía comanda A
//	 * @param linB {@link LiniaComandaEDI} Linía comanda A
//	 * @return resultat de l'stock reservat de la linA i la linB
//	 */
//	static long diferenciaStockReservat(LiniaComandaEDI linA, LiniaComandaEDI linB) {
//		var stockReservatOriginal = linA.reserva().map(InformacioReserva::quantitat).orElse(0L);
//		var stockReservatActualitzat = linB.reserva().map(InformacioReserva::quantitat).orElse(0L);
//		return stockReservatActualitzat-stockReservatOriginal;
//	}
//
}
