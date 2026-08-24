package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CI;

public class CiWrapper {

    private final CI raw;

    public CiWrapper(CI raw) {
        this.raw = raw;
    }

    public String idComprador() {
        return raw.getIdComprador();
    }

    public String idProveedor() {
        return raw.getIdProveedor();
    }

    public String idExpedidor() {
        return raw.getIdExpedidor();
    }

    public String numCuentaInterna() { return raw.getNumCuentaInternaProveedor(); }
}
