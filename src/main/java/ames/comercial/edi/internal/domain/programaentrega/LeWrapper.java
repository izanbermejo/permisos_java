package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LE;

public class LeWrapper {
    private final LE raw;

    public LeWrapper(LE raw) { this.raw = raw; }

    public String tipoBulto()            { return raw.getTipoBulto() != null ? raw.getTipoBulto().trim() : ""; }
    public String referenciaEmbalaje()   { return raw.getReferenciaEmbalaje() != null ? raw.getReferenciaEmbalaje().trim() : ""; }
    public String piezasPorEmbalaje()    { return raw.getPiezasPorEmbalaje() != null ? raw.getPiezasPorEmbalaje().trim() : ""; }
    public String numeroEmbalajes()      { return raw.getNumeroEmbalajes() != null ? raw.getNumeroEmbalajes().trim() : ""; }
    public String nivelEmpaquetamiento() { return raw.getNivelEmpaquetamiento() != null ? raw.getNivelEmpaquetamiento().trim() : ""; }
}
