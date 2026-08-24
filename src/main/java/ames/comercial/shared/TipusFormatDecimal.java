package ames.comercial.shared;

import java.text.DecimalFormatSymbols;

public enum TipusFormatDecimal {

    ESPAI_COMA(buildDecimalFormatSymbol(' ', ',')),
    COMA_PUNT(buildDecimalFormatSymbol(',','.')),
    PUNT_COMA(buildDecimalFormatSymbol('.',','));

    private final DecimalFormatSymbols decimalFormatSymbols;

    TipusFormatDecimal(DecimalFormatSymbols decimalFormatSymbols) {
        this.decimalFormatSymbols = decimalFormatSymbols;
    }

    public DecimalFormatSymbols decimalFormatSymbols() {
        return decimalFormatSymbols;
    }

    private static DecimalFormatSymbols buildDecimalFormatSymbol(char separadorMilers, char separadorDecimals) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(separadorMilers);
        symbols.setPerMill(separadorMilers);
        symbols.setDecimalSeparator(separadorDecimals);
        return symbols;
    }
}
