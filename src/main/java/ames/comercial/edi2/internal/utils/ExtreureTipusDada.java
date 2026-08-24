package ames.comercial.edi2.internal.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;

public class ExtreureTipusDada {

    private static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyyMMdd"))
            .toFormatter();

    private static final DateTimeFormatter HORA_FORMAT = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("HH:mm"))
            .appendOptional(DateTimeFormatter.ofPattern("HHmm"))
            .toFormatter();

    protected static String extreureText(String linia, int inici, int fi) {
        return linia.substring(inici, fi).trim();
    }

    protected static Optional<String> extreureOpcional(String linia, int inici, int fi) {
        String valor = extreureText(linia, inici, fi);
        return valor.isBlank() ? Optional.empty() : Optional.of(valor);
    }

    protected static Optional<Long> extreureLongOptional(String linia, int inici, int fi) {
        String valor = extreureText(linia, inici, fi);
        if (valor.isBlank()) return Optional.empty();
        return Optional.of(Long.valueOf(valor));
    }

    protected static Optional<BigDecimal> extreureBigDecimalOptional(String linia, int inici, int fi) {
        String valor = extreureText(linia, inici, fi);
        if (valor.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(valor));
    }

    protected static Long extreureLong(String linia, int inici, int fi) {
        String valor = extreureText(linia, inici, fi);
        return Long.valueOf(valor);
    }

    protected static LocalDate extreureDate(String linia, int inici, int fi){
        String valor = linia.substring(inici, fi).trim();
        return LocalDate.parse(valor, DATE_FORMAT);
    }

    protected static Optional<LocalDate> extreureDateOptional(String linia, int inici, int fi) {
        String valor = linia.substring(inici, fi).trim();
        if (valor.isBlank() || valor.equals("//")) return Optional.empty();
        return Optional.of(LocalDate.parse(valor, DATE_FORMAT));
    }

    protected static Optional<LocalTime> extreureHoraOptional(String linia, int inici, int fi) {
        String valor = linia.substring(inici, fi).trim();
        if (valor.isBlank()) return Optional.empty();
        return Optional.of(LocalTime.parse(valor, HORA_FORMAT));
    }
}
