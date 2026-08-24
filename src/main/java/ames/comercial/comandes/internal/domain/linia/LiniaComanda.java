package ames.comercial.comandes.internal.domain.linia;

import ames.comercial.comandes.ComandesException.NovaQuantitatInferiorServida;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static ames.comercial.shared.Numbers.decimal;
import static ames.comercial.shared.Numbers.descompteAplicar;

@JsonDeserialize(builder = LiniaComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaComanda {

	KeyLiniaComanda id();
	KeyArticleClient articleClient();
	TipusArticleClient tipusArticleClient();
	String referencia();
	TipusLiniaComanda tipus();
	long quantitat();
	Preu preu();
	@Default default boolean isPreuFixat() { return  false; }
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Optional<LocalDate> dataPrevistaSortidaInterna();
	Optional<LocalDate> dataConfirmadaFabrica();
	long quantitatServida();
	Optional<InformacioReserva> reserva();
	Optional<DadesCalculNormalitzat> dadesCalcul();
	Optional<Long> comandaBlanca();
	Optional<String> usuari();
	/**
	 * @return Data en la que es va crear la línia (no es modifica quan s'edita) i que únicament
	 * es fa servir per al càlcul de reserves (tenen prioritat les que es van crear primer)
	 */
	LocalDateTime dataCreacio();

	@Derived
	default long comanda() {
		return id().comanda();
	}

	@Derived
	default long numero() {
		return id().numero();
	}

	@Derived
	default BigDecimal importNet() {
		return preu().imp(quantitat())
				.multiply(descompteAplicar(dadesCalcul().map(DadesCalculNormalitzat::descompte).orElse(BigDecimal.ZERO)))
				.divide(decimal(100), 3, RoundingMode.HALF_UP);
	}

	@Derived
	default BigDecimal importBrut() {
		return preu().imp(quantitat());
	}

	@Derived
	default long quantitatPendent() {
		return Math.max(0, quantitat() - quantitatServida());
	}

	@Derived
	default boolean servida() {
		return quantitatPendent() == 0;
	}

	@Derived
	default long quantitatReservada() { return reserva().map(InformacioReserva::quantitat).orElse(0L); }

	/**
	 * @return Quantitat de peces pendents de reservar de la línia
	 */
	@Derived
	default long quantitatPendentReservar() { return quantitatPendent() - quantitatReservada(); }

	/**
	 * @return True quan la línia té peces pendents de reservar, false altrament
	 */
	@Derived
	default boolean teQuantitatPendentReservar() { return quantitatPendentReservar() > 0; }

	/**
	 * @return True quan la línia es d'stock de seguretat (ja sigui de client o interna d'AMES)
	 */
	@Derived
	default boolean isStockSeguretat() { return tipus().isStockSeguretat(); }

	default LiniaComanda canviarDates(LocalDate dataSolicitada, LocalDate dataPrevistaSortida) {
		return LiniaComandaImpl.builder()
				.from(this)
				.dataSolicitada(dataSolicitada)
				.dataPrevistaSortida(dataPrevistaSortida)
				.build();
	}

	default LiniaComanda traspas(KeyLiniaComanda clauLinia) {
		return LiniaComandaImpl.builder().from(this)
				.id(clauLinia)
				.quantitat(quantitatPendent())
				.quantitatServida(0)
				.build();
	}

	default LiniaComanda canviarQuantitat(long novaQuantitat, long reservaDisponible, Preu nouPreu, long quantitatCalcul) {
		// La nova quantitat no pot ser inferior a les peces ja servides
		if (novaQuantitat < quantitatServida())
			throw new NovaQuantitatInferiorServida(novaQuantitat, quantitatServida());
		// En cas que no s'hagi tocat la quantitat no fem res
		if (novaQuantitat == quantitat())
			return this;
		// Diferència de quantitat
		var difQtat = novaQuantitat - quantitat();
		// Línia amb la quantitat actualitzada
		LiniaComanda newLinia = LiniaComandaImpl.builder()
				.from(this)
				.quantitat(novaQuantitat)
				.build();
		if (difQtat > 0) {
			// En cas que es demanin mes peces s'augmenta la reserva
			// entre el mínim de quantitat pendent o la reserva disponible
			newLinia = newLinia.afegirReserva(Math.min(difQtat + quantitatPendentReservar(), reservaDisponible));
		} else {
			// En cas que es demanin menys peces es decrementa la reserva
			// entre màxim 0 i la diferència entre la nova quantitat i la quantitat ja reservada
			newLinia = newLinia.treureReserva(Math.max(0, quantitatReservada()-novaQuantitat));
		}
		// Actualització del nou preu i de la quantitat de càlcul
		return LiniaComandaImpl.builder()
				.from(newLinia)
				.preu(nouPreu)
				.dadesCalcul(DadesCalculNormalitzatImpl.builder()
						.from(dadesCalcul().orElseThrow())
						.quantitatCalcul(quantitatCalcul)
						.build())
				.quantitat(novaQuantitat)
				.build();
	}

	/**
	 * @param qtatTreure Qtat de peces a treure de la reserva
	 * @return Actualitza la informació de reserva de la línia després de treure la quantitat de peces
	 * de la reserva
	 */
	default LiniaComanda treureReserva(long qtatTreure) {
		var oldReserva = reserva().orElseThrow();
		var newQtat = oldReserva.quantitat() - qtatTreure;
		var newReserva = InformacioReserva.of(quantitatPendent(), newQtat);
		return LiniaComandaImpl.builder()
				.from(this)
				.reserva(newReserva)
				.build();
	}

	default LiniaComanda afegirReserva(long qtatAfegir) {
		var oldReserva = reserva().orElseThrow();
		var newQtat = oldReserva.quantitat() + qtatAfegir;
		var newReserva = InformacioReserva.of(quantitatPendent(), newQtat);
		return LiniaComandaImpl.builder()
				.from(this)
				.reserva(newReserva)
				.build();
	}

	default LiniaComanda actualitzarReserva(long qtatReservada) {
		var newReserva = InformacioReserva.of(quantitatPendent(), qtatReservada);
		return LiniaComandaImpl.builder()
				.from(this)
				.reserva(newReserva)
				.build();
	}

	@Derived
	public default String codiFormat() { return String.format("%07d", id().comanda()); }

	@Derived
	public default String numeroFormat() {
		return String.format("%04d", id().numero());
	}

	@Derived
	public default String codiNumeroFormat() {
		return codiFormat() + " / " + numeroFormat();
	}

	default LiniaComanda servir (long quantitatServir) {
		Optional<InformacioReserva> newInfoReserva = Optional.empty();
		// En cas de normalitzats s'ha de tornar a calcular la informació de la reserva
		if (tipusArticleClient().isSistemaReserva()) {
			var infoReserva = reserva().orElseThrow();
			var novaQuantitatReservada = Math.max(0, infoReserva.quantitat() - quantitatServir);
			var novaQuantitatPendent = Math.max(0, quantitatPendent() - quantitatServir);
			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
					.quantitat(novaQuantitatReservada)
					.estat(Reservable.of(novaQuantitatReservada, novaQuantitatPendent))
					.build());
		}
		return LiniaComandaImpl.builder().from(this)
				.quantitatServida(quantitatServida()+quantitatServir)
				.reserva(newInfoReserva)
				.build();
	}

	default LiniaComanda desfer (long quantitatDesfer) {
		Optional<InformacioReserva> newInfoReserva = Optional.empty();
		// En cas que no sigui una peça especial cal refer la informació de reserva
		if (tipusArticleClient().isSistemaReserva()) {
			var infoReserva = reserva().orElseThrow();
			var novaQuantitatReservada = infoReserva.quantitat() + quantitatDesfer;
			var novaQuantitatPendent = quantitatPendent() + quantitatDesfer;
			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
					.quantitat(novaQuantitatReservada)
					.estat(Reservable.of(novaQuantitatReservada, novaQuantitatPendent))
					.build());
		}
		var novaQuantitatServida = Math.max(quantitatServida() - quantitatDesfer, 0);
		return LiniaComandaImpl.builder().from(this)
				.quantitatServida(novaQuantitatServida)
				.reserva(newInfoReserva)
				.build();
	}

	default LiniaComanda cancelar() {
		// En cas d'estar servida no cal fer res
		if (servida())
			return this;
		// La quantitat passa a ser la servida i es treu tota la reserva
		return LiniaComandaImpl.builder().from(this)
				.quantitat(quantitatServida())
				.reserva(InformacioReserva.empty())
				.build();
	}

	default LiniaComanda eliminar() {
		if (quantitatServida() > 0)
			throw new AppException("No es pot eliminar una línia on ja s'han servit peces");
		Optional<InformacioReserva> newInfoReserva = Optional.empty();
		// En cas de normalitzats s'ha de tornar a calcular la informació de la reserva
		if (TipusArticleClient.NORMALITZAT.equals(tipusArticleClient())) {
			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
					.quantitat(0)
					.estat(Reservable.NO_APLICA)
					.build());
		}
		return LiniaComandaImpl.builder().from(this)
				.quantitat(0)
				.reserva(newInfoReserva)
				.build();
	}

	default LiniaComanda canviarFermOrientatiu() {
		// En cas que el tipus de la línia sigui ferm es passa a ORIENTATIU,
		// en la resta de casos es passa a FERM
		TipusLiniaComanda tipusCalculat = tipus().equals(TipusLiniaComanda.FERM)
				? TipusLiniaComanda.ORIENTATIU
				: TipusLiniaComanda.FERM;

		return LiniaComandaImpl.builder()
				.from(this)
				.tipus(tipusCalculat)
				.build();
	}

	default  LiniaComanda fixarPreu(Preu preu) {
		// Dades càlcul a 0
		var dadesCalcul = DadesCalculNormalitzatImpl.builder()
				.from(dadesCalcul().orElseThrow())
				.quantitatCalcul(0)
				.build();
		return LiniaComandaImpl.builder()
				.from(this)
				.isPreuFixat(true)
				.preu(preu)
				.dadesCalcul(dadesCalcul)
				.build();
	}

	default LiniaComanda desfixarPreu(Preu preu, BigDecimal descompte, long quanitatCalcul) {
		var dadesCalcul = DadesCalculNormalitzatImpl.builder()
				.from(dadesCalcul().orElseThrow())
				.quantitatCalcul(quanitatCalcul)
				.descompte(descompte)
				.build();
		return LiniaComandaImpl.builder()
				.from(this)
				.isPreuFixat(false)
				.preu(preu)
				.dadesCalcul(dadesCalcul)
				.build();
	}

	default LiniaComanda anular (){
		Optional<InformacioReserva> newInfoReserva = Optional.empty();
		// En cas de normalitzats s'ha de tornar a calcular la informació de la reserva
		if (TipusArticleClient.NORMALITZAT.equals(tipusArticleClient())) {
			newInfoReserva = Optional.of(InformacioReservaImpl.builder()
					.quantitat(0)
					.estat(Reservable.NO_APLICA)
					.build());
		}
		return LiniaComandaImpl.builder().from(this)
				.quantitat(quantitatServida())
				.reserva(newInfoReserva)
				.build();
	}

	default LiniaComanda actualitzacioPreu (BigDecimal preu, Divisa divisa) {
		return LiniaComandaImpl.builder()
				.from(this)
				.preu(Preu.of(preu, divisa))
				.build();
	}

	/**
	 * Càlcula la diferència d'stock reserva entre dos línies de comanda
	 * @param linA {@link LiniaComanda} Linía comanda A
	 * @param linB {@link LiniaComanda} Linía comanda A
	 * @return resultat de l'stock reservat de la linA i la linB
	 */
	static long diferenciaStockReservat(LiniaComanda linA, LiniaComanda linB) {
		var stockReservatOriginal = linA.reserva().map(InformacioReserva::quantitat).orElse(0L);
		var stockReservatActualitzat = linB.reserva().map(InformacioReserva::quantitat).orElse(0L);
		return stockReservatActualitzat-stockReservatOriginal;
	}

}