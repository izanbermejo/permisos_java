package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LD;

import java.util.Optional;

public class LdWrapper {

    private final LD raw;

    public LdWrapper(LD raw) {
        this.raw = raw;
    }

    public Optional<String> direccion() {
        return opt(raw.getDireccionConsignatario());
    }

    public Optional<String> localidad() {
        return opt(raw.getLocalidadConsignatario());
    }

    public Optional<String> provincia() {
        return opt(raw.getProvinciaConsignatario());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
