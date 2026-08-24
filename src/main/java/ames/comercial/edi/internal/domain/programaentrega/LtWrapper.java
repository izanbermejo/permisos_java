package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LT;

import java.util.Optional;

public class LtWrapper {

    private final LT raw;

    public LtWrapper(LT raw) {
        this.raw = raw;
    }

    public Optional<String> texto1() {
        return opt(raw.getTexto1());
    }

    public Optional<String> texto2() {
        return opt(raw.getTexto2());
    }

    public Optional<String> texto3() {
        return opt(raw.getTexto3());
    }

    public Optional<String> texto4() {
        return opt(raw.getTexto4());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
