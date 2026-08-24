package ames.comercial.albarans.internal.services.aviexp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AviExpUtils {

    public static String espais(int n) {
        return " ".repeat(n);
    }

    public static String alfanumeric(String valor, int longitud) {
        // En cas que el valor sigui null, el convertim a cadena buida
        if (valor == null) {
            valor = "";
        }
        if (valor.length() > longitud) {
            // Es trunca l'string si el valor és més llarg que la longitud especificada
            return valor.substring(0, longitud);
        } else {
            // Es justifica el valor a l'esquerra i s'afegeixen espais a la dreta fins a completar la longitud especificada
            return valor + " ".repeat(longitud - valor.length());
        }
    }

    public static String alfanumeric(long valor, int longitud) {
        return alfanumeric(String.valueOf(valor), longitud);
    }

    public static String numeric(long valor, int longitud) {
        // S'afegeixen zeros a l'esquerra fins a completar la longitud especificada
        String valorStr = String.valueOf(valor);
        return "0".repeat(longitud - valorStr.length()) + valorStr;
    }

    public static String numeric(BigDecimal valor, int longitud) {
        // Convertim el valor a cèntims (multiplicant per 1000) i el convertim a long
        // Per exemple, si el valor és 123.45, el valor en cèntims serà 12345 ja que s'han d'afegir tres decimals al
        // valor i eliminar el punt decimal
        long valorCentims = valor.multiply(BigDecimal.valueOf(1000)).longValue();
        return numeric(valorCentims, longitud);
    }

    public static String data(LocalDate data) {
        // Format de data: DD/MM/AAAA
        return String.format("%02d/%02d/%04d", data.getDayOfMonth(), data.getMonthValue(), data.getYear());
    }

    public static String hora(LocalTime hora) {
        // Format d'hora: HH:MM
        return String.format("%02d:%02d", hora.getHour(), hora.getMinute());
    }

}
