package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LB;

import java.util.Optional;

public class LbWrapper {

    private final LB raw;

    public LbWrapper(LB raw) {
        this.raw = raw;
    }

    public Optional<String> identificacionArticuloProveedor() {
        return opt(raw.getIdArticuloProveedor());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
