package ames.comercial.inventari;

import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;

@SuppressWarnings("serial")
public class InventariException {

	public static class StockReservableInsuficientException extends AppException {
		public StockReservableInsuficientException() {
			super(I18N.getLiteral("stock_reservable_insuficient"));
		}
	}

	public static class MovimentNoTrobat extends AppException {
		public MovimentNoTrobat() {
			super(I18N.getLiteral("moviment_no_trobat"));
		}
	}

	public static class NoEsRegularitzacio extends AppException {
		public NoEsRegularitzacio() {
			super(I18N.getLiteral("moviment_no_es_regularitzacio"));
		}
	}

	public static class NoEsFerralla extends AppException {
		public NoEsFerralla() {
			super(I18N.getLiteral("moviment_no_es_ferralla"));
		}
	}

	public static class QuantitatHaDeSerPositiva extends AppException {
		public QuantitatHaDeSerPositiva() {
			super(I18N.getLiteral("quantitat_ha_de_ser_positiva"));
		}
	}

	public static class ErrorEnviantCorreuLlistatImmobilitzades extends AppException {
		public ErrorEnviantCorreuLlistatImmobilitzades(Throwable cause) {
			super(I18N.getLiteral("error_enviant_correu_llistat_immobilitzades"), cause);
		}
	}

	public static class ErrorEnviantCorreuLlistatFerralla extends AppException {
		public ErrorEnviantCorreuLlistatFerralla(Throwable cause) {
			super(I18N.getLiteral("error_enviant_correu_llistat_ferralla"), cause);
		}
	}

}
