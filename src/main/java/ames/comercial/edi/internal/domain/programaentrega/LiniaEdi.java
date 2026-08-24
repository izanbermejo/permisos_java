package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.Detalle;

public class LiniaEdi {

    private final Detalle raw;

    public LiniaEdi(Detalle raw) {
        this.raw = raw;
    }

    public DaWrapper da() {
        return new DaWrapper(raw.getDA());
    }
}
