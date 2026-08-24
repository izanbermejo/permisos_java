package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CP;

import java.util.Optional;

public class CpWrapper {

    private final CP raw;

    public CpWrapper(CP raw) {
        this.raw = raw;
    }

    public Optional<String> nombreProveedor() {
        return opt(raw.getNombreProveedor());
    }

    public Optional<String> direccionProveedor() {
        return opt(raw.getDireccionProveedor());
    }

    public Optional<String> localidadProveedor() {
        return opt(raw.getLocalidadProveedor());
    }

    public Optional<String> codPostalProveedor() {
        return opt(raw.getCodigoPostalProveedor());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
