package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CT;

public class CtWrapper {

    private final CT raw;

    public CtWrapper(CT raw) {
        this.raw = raw;
    }

    public String textoLibre() {
        return raw.getTextoLibre();
    }
}
