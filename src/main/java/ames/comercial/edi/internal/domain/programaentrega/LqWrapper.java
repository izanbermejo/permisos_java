package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LQ;

import java.util.Optional;

public class LqWrapper {

    private final LQ raw;

    public LqWrapper(LQ raw) {
        this.raw = raw;
    }

    public Optional<Long> cantidadAcumuladaRecibida() {
        return parseLong(raw.getCantidadAcumuladaRecibida());
    }

    public Optional<Long> cantidadAcumuladaProgramada() {
        return parseLong(raw.getCantidadAcumuladaProgramada());
    }

    public Optional<Long> cantidadAtraso() {
        return parseLong(raw.getCantidadAtraso());
    }

    public Optional<String> fechaCantidadAtraso() {
        return EdiDateParser.parse(raw.getFechaCantidadAtraso());
    }

    private Optional<Long> parseLong(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(s.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
