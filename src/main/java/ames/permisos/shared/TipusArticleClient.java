package ames.permisos.shared;

import java.util.HashMap;
import java.util.Map;

public enum TipusArticleClient {

	ESPECIAL ("E", false, false),
	FILTRE ("F", true, true),
	MARKETING ("K", false, true),
	MEDICAL ("M", false, true),
	NORMALITZAT ("N", true, true),
	IBINSA ("I", false, false);

	private static final Map<String, TipusArticleClient> LOOKUP_MAP;
	private final String clauAdvantage;
	private final boolean isAplicaDescompte;
	private final boolean isSistemaReserva;

	TipusArticleClient(String clauAdvantage, boolean isAplicaDescompte, boolean isSistemaReserva) {
		this.clauAdvantage = clauAdvantage;
		this.isAplicaDescompte = isAplicaDescompte;
		this.isSistemaReserva = isSistemaReserva;
	}
	
	public String clauAdvantage () {
		return clauAdvantage;
	}

	public boolean isAplicaDescompte () { return isAplicaDescompte; }

	public boolean isSistemaReserva () { return isSistemaReserva; }

	static {
		LOOKUP_MAP = new HashMap<String, TipusArticleClient>();
		for (TipusArticleClient t : TipusArticleClient.values()) {
			LOOKUP_MAP.put(t.clauAdvantage, t);
		}
	}

	public static TipusArticleClient getByTipus(String value) {
		var f = LOOKUP_MAP.get(value);
		return f != null ? f : TipusArticleClient.ESPECIAL;
	}
	
}
