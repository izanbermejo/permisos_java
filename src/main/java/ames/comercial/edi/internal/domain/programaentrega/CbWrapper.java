package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CB;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CbWrapper {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final CB raw;

    public CbWrapper(CB raw) {
        this.raw = raw;
    }

    public String fechaMensaje() {
        return EdiDateParser.parse(raw.getFechaMensaje()).orElse("");
    }

    public Optional<LocalTime> horaMensaje() {
        String s = raw.getHoraMensaje();
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(LocalTime.parse(s.trim(), TIME_FMT));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> idTransportista() {
        return opt(raw.getIdTransportista());
    }

    public Optional<String> fechaInicioHorizonte() {
        return EdiDateParser.parse(raw.getFechaInicioHorizonte());
    }

    public Optional<String> fechaFinalHorizonte() {
        return EdiDateParser.parse(raw.getFechaFinalHorizonte());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
