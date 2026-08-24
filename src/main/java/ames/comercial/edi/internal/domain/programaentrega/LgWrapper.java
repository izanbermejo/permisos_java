package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LG;

import java.util.Optional;

public class LgWrapper {

    private final LG raw;

    public LgWrapper(LG raw) {
        this.raw = raw;
    }

    public Optional<String> descripcionArticulo() {
        return opt(raw.getDescripcionArticulo());
    }

    public String numeroContratoPedido() {
        return raw.getNumeroContrato();
    }

    public Optional<String> fechaContratoPedido() {
        return EdiDateParser.parse(raw.getFechaContrato());
    }

    public Optional<String> numeroLineaContrato() {
        return opt(raw.getNumeroLineaContrato());
    }

    public Optional<String> numeroPlano() {
        return opt(raw.getNumeroDePlano());
    }

    public Optional<String> numeroDocumentoAnterior() {
        return opt(raw.getNumeroDocumentoAnterior());
    }

    public Optional<String> fechaDocumentoAnterior() {
        return EdiDateParser.parse(raw.getFechaDocumentoAnterior());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
