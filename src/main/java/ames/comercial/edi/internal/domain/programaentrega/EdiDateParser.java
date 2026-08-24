package ames.comercial.edi.internal.domain.programaentrega;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

class EdiDateParser {

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter YYYYMMDD   = DateTimeFormatter.ofPattern("yyyyMMdd");

    static Optional<String> parse(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String s = raw.trim();
        try { LocalDate.parse(s, DD_MM_YYYY); return Optional.of(s); }
        catch (DateTimeParseException ignored) {}
        try { return Optional.of(LocalDate.parse(s, YYYYMMDD).format(DD_MM_YYYY)); }
        catch (DateTimeParseException ignored) {}
        return Optional.of(s);
    }

    private EdiDateParser() {}
}
