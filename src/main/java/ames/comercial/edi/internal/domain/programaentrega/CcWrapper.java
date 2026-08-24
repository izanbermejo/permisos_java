package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CC;

import java.util.Optional;

public class CcWrapper {

    private final CC raw;

    public CcWrapper(CC raw) {
        this.raw = raw;
    }

    public String nombreComprador() {
        return raw.getNombreComprador();
    }

    public Optional<String> direccionComprador() {
        return opt(raw.getDireccionComprador());
    }

    public Optional<String> localidadComprador() {
        return opt(raw.getLocalidadComprador());
    }

    public Optional<String> codigoPostalComprador() {
        return opt(raw.getCodigoPostalComprador());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
