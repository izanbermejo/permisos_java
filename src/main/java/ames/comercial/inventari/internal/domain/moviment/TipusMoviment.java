package ames.comercial.inventari.internal.domain.moviment;

public enum TipusMoviment {

	ENTRADA (1),
	SORTIDA (2),
	REGULARITZACIO (3),
	TRASPAS_MAGATZEM (4),
	FERRALLA (5),
	TRASPAS_CLIENT (6),
	TRASPAS_EMPRESA (7),
	COMPRA_EXISTENCIES (8);

	private final int clauAdvantage;

	TipusMoviment(int clauAdvantage) {
		this.clauAdvantage = clauAdvantage;
	}

	public static TipusMoviment fromClauAdvantage(int clauAdvantage) {
		for (TipusMoviment tipus : values()) {
			if (tipus.clauAdvantage == clauAdvantage) {
				return tipus;
			}
		}
		throw new IllegalArgumentException("Tipus de moviment desconegut per la clau d'Advantage: " + clauAdvantage);
	}

	/**
	 * Quantitat amb el signe que li correspon quan es calcula l'estoc d'una fitxa: les sortides
	 * el redueixen i la resta de tipus l'incrementen. El signe intern de la quantitat ja
	 * distingeix els casos inversos de cada tipus (una SORTIDA negativa és una devolució de
	 * client, una ENTRADA negativa és una devolució a fàbrica), per això no cal mirar-lo aquí.
	 */
	public long quantitatCalculFitxa(long quantitat) {
		return this == SORTIDA ? -quantitat : quantitat;
	}

}
