package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CA;

import java.util.Optional;

public class CaWrapper {

    private final CA raw;

    public CaWrapper(CA raw) {
        this.raw = raw;
    }

    public String buzonOrigen() {
        return raw.getBuzonOrigen();
    }

    public String buzonDestino() {
        return raw.getBuzonDestino();
    }

    public String numeroDocumento() {
        return raw.getNumeroDocumento();
    }

    public String codigoDocumento() {
        return raw.getCodigoDocumento();
    }

    public Optional<String> logisticaNombreMensaje() {
        return opt(raw.getLogistica());
    }

    public Optional<String> funcionMensaje() {
        return opt(raw.getFuncion());
    }

    public Optional<String> referenciaAplicacion() {
        return opt(raw.getReferencia());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
