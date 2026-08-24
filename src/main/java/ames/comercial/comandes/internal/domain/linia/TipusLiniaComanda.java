package ames.comercial.comandes.internal.domain.linia;

public enum TipusLiniaComanda {

	FERM ("F", false),
	ORIENTATIU ("O", false),
	INVENT ("I", false),
	STOCK_SEG_CLIENT ("S", true),
	STOCK_SEG_AMES ("S", true);
	
	private final String clauAdvantage;
	private final boolean stockSeguretat;
	
	TipusLiniaComanda (String clauAdvantage, boolean stockSeguretat) {
		this.clauAdvantage = clauAdvantage;
		this.stockSeguretat = stockSeguretat;
	}
	
	public String clauAdvantage () {
		return clauAdvantage;
	}

	public boolean isStockSeguretat () { return stockSeguretat; }
	
}
