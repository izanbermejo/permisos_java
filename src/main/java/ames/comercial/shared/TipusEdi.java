package ames.comercial.shared;

import java.util.HashMap;
import java.util.Map;

public enum TipusEdi {

    DELFOR("DELFOR", "FERM"),
    DELINS("DELINS", "FERM"),
    DELJIT("DELJIT", "ORIENTATIU"),
    ORDERR("ORDERR", "ORIENTATIU");

    private static final Map<String, TipusEdi> LOOKUP_MAP;

    private final String codi;
    private final String tipusLinia;

    TipusEdi(String codi, String tipusLinia) {
        this.codi = codi;
        this.tipusLinia = tipusLinia;
    }

    public String codi() {
        return codi;
    }

    public String tipusLinia() {
        return tipusLinia;
    }

    static {
        LOOKUP_MAP = new HashMap<>();
        for (TipusEdi t : TipusEdi.values()) {
            LOOKUP_MAP.put(t.codi, t);
        }
    }

    public static TipusEdi from(String value) {
        TipusEdi tipus = LOOKUP_MAP.get(value);
        if (tipus == null) {
            throw new IllegalArgumentException("Tipus EDI no suportat: " + value);
        }
        return tipus;
    }

    public static String toTipusLinia(String value) {
        return from(value).tipusLinia();
    }
}
