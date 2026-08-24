package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LA;

import java.util.Optional;

public class LaWrapper {

    private final LA raw;

    public LaWrapper(LA raw) {
        this.raw = raw;
    }

    public String idArticuloComprador() {
        return raw.getIdArticuloComprador();
    }

    public Optional<String> lugarEntrega() {
        return opt(raw.getLugarEntrega());
    }

    public Optional<String> codigoAlmacen() {
        return opt(raw.getCodigoAlmacen());
    }

    public Optional<String> destinoFinal() {
        return opt(raw.getLugarDestinoFinal());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
