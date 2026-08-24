package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.AA;

import java.util.Optional;

public class AaWrapper {

    private final AA raw;

    public AaWrapper(AA raw) {
        this.raw = raw;
    }

    public Optional<String> referenciaAlbaranEntrada() {
        return opt(raw.getReferenciaAlbaranEntrada());
    }

    public Optional<String> fechaAlbaran() {
        return EdiDateParser.parse(raw.getFechaAlbaran());
    }

    public Optional<String> cantidadEnviadaAlbaran() {
        return opt(raw.getCantidadEnviadaAlbaran());
    }

    public Optional<String> cantidadRecibidaAlbaran() {
        return opt(raw.getCantidadRecibidaAlbaran());
    }

    public Optional<String> fechaRecepcion() {
        return EdiDateParser.parse(raw.getFechaRecepcion());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
