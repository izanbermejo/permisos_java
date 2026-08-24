package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.CD;

import java.util.Optional;

public class CdWrapper {

    private final CD raw;

    public CdWrapper(CD raw) {
        this.raw = raw;
    }

    public Optional<String> personaContactoComprador() {
        return opt(raw.getPersonaContactoComprador());
    }

    private Optional<String> opt(String s) {
        return Optional.ofNullable(s).filter(x -> !x.isBlank());
    }
}
