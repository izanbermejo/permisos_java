package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.DA;

import java.util.Optional;

public class DaWrapper {

    private final DA raw;

    public DaWrapper(DA raw) {
        this.raw = raw;
    }

    public String tipoDetalle() {
        return raw.getTipo();
    }

    public long cantidad() {
        try {
            return Long.parseLong(raw.getCantidad().trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    public String unidadMedida() {
        return raw.getUnidadMedida();
    }

    public Optional<String> fechaInicial() {
        return EdiDateParser.parse(raw.getFechaInicial());
    }

    public Optional<String> fechaFinal() {
        return EdiDateParser.parse(raw.getFechaFinal());
    }

    public Optional<String> razonInstruccion() {
        return opt(raw.getRazonInstruccion());
    }

    public Optional<String> frecuenciaEnvio() {
        return opt(raw.getFrecuenciaEnvio());
    }

    public Optional<String> numeroRan() {
        return opt(raw.getNumeroRAN());
    }

    public Optional<String> ultimoNumeroRanEmitido() {
        return opt(raw.getUltimoNumeroRAN());
    }

    public Optional<String> numTarjetaKanban() {
        return opt(raw.getNumeroTarjetaKanban());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
