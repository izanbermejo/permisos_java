package ames.comercial.shared;

import ames.comercial.server.exception.AppException;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public enum Empresa {

    FUSIOMOL ("00", "0000"),
    BARCELONA ("10","0001"),
    MONTBLANC ("20", "0001"),
    TOOLING ("30","0001"),
    GROUP ("40", "0001"),
    TAMARITE ("50","0001"),
    SOLSONA ("60","0001"),
    REESE ("70", "0070"),
    MONTERREY ("80", "0080"),
    CMA ("90", "0001"),
    HUNGARIA ("A0","0038"),
    WUHU ("B0","0040"),
    PM ("C0","0001"),
    MEDICAL ("D0", "0045"),
    PORE ("F0", "0001"),
    AMES ("Z0","0001");


    private static final Map<String, Empresa> LOOKUP_MAP;
    private static final Map<Empresa, Empresa> REFERENCIA_STOCK;
    private final String clau;
    private final String magatzem;

    Empresa(String clau, String magatzem) {
        this.clau = clau;
        this.magatzem = magatzem;
    }

    public String clau () {
        return clau;
    }

    public String magatzem () { return  magatzem; }

    static {
        LOOKUP_MAP = new HashMap<String, Empresa>();
        for (Empresa e : Empresa.values()) {
            LOOKUP_MAP.put(e.clau, e);
        }
    }

    static {
        REFERENCIA_STOCK = new HashMap<>();
        REFERENCIA_STOCK.put(MONTERREY, MONTERREY);
        REFERENCIA_STOCK.put(MEDICAL, MEDICAL);
    }

    public static Empresa getByClau(String value) {
        var f = LOOKUP_MAP.get(value);
        if (f == null)
            throw new AppException(MessageFormat.format("Empresa {0} no té correspondència", value));
        return f;
    }

    public static Empresa referenciaStock(Empresa empresa) {
        var e = REFERENCIA_STOCK.get(empresa);
        return e != null ? e : GROUP;
    }

}
