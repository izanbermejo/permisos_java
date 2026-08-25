package ames.permisos.shared;

import ames.permisos.server.exception.AppException;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Divisa {

    EURO ("EUR"),
    EURO_CENT (EURO),
    DOLAR ("USD"),
    DOLAR_CENT (DOLAR),
    YUAN ("CNY"),
    YUAN_CENT (YUAN),
    LLIURA_ESTERLINA ("GBP"),
    LLIURA_ESTERLINA_CENT (LLIURA_ESTERLINA),
    PES_MEXIC ("MXN"),
    PES_MEXIC_CENT (PES_MEXIC),
    ANTIC_PES_MEXIC ("MXP"),
    ANTIC_PES_MEXIC_CENT (ANTIC_PES_MEXIC),
    YEN ("JPY"),
    YEN_CENT (YEN),
    FRANC_ALEMANY("DEM"),
    FRANC_ALEMANY_CENT(FRANC_ALEMANY),
    PESETA("ESP"),
    PESETA_CENT(PESETA),
    FRANC_FRANCES("FRF"),
    FRANC_FRANCES_CENT(FRANC_ALEMANY),
    LLIURA_ITALIANA("ITL"),
    LLIURA_ITALIANA_CENT(LLIURA_ITALIANA),
    FLORI_HOLANDES("NLG"),
    FLORI_HOLANDES_CENT(FLORI_HOLANDES),
    FLORI_HONGARES("HUF"),
    FLORI_HONGARES_CENT(FLORI_HONGARES),
    FRANC_SUIS("CHF"),
    FRANC_SUIS_CENT(FRANC_SUIS);

    private static final Map<String, Divisa> LOOKUP_MAP;
    private final String symbol;
    private final boolean isCent;
    private final Optional<Divisa> base;

    Divisa (Divisa base) {
        this.symbol = base.symbol + "%";
        this.isCent = true;
        this.base = Optional.of(base);
    }

    Divisa (String symbol) {
        this.symbol = symbol;
        this.isCent = false;
        this.base = Optional.empty();
    }

    public String symbol() {
        return symbol;
    }

    public boolean isCent() {
        return isCent;
    }

    public Divisa base() {
        return base.orElse(this);
    }

    static {
        LOOKUP_MAP = new HashMap<String, Divisa>();
        for (Divisa d : Divisa.values()) {
            LOOKUP_MAP.put(d.symbol, d);
        }
    }

    public static Divisa getBySymbol(String symbol) {
        var d = LOOKUP_MAP.get(symbol);
        if (d == null)
            throw new AppException(MessageFormat.format("La divisa \"{0}\" no té correspondència", symbol));
        return d;
    }

    @JsonValue
    @Override
    public String toString() {
        return symbol;
    }
}
