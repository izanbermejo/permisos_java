package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LC;

public class LcWrapper {

    private final LC raw;

    public LcWrapper(LC raw) {
        this.raw = raw;
    }

    public String codigoConsignatario() {
        return raw.getCodigoConsignatario();
    }

    public String nombreConsignatario() {
        return raw.getNombreConsignatario();
    }
}
