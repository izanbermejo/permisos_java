package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LS;

public class LsWrapper {

    private final LS raw;

    public LsWrapper(LS raw) {
        this.raw = raw;
    }

    public String solicitante () {
        return raw.getCodigoSolicitante();
    }

}
