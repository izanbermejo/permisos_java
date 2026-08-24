package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.ComandaMissatgeEDI;

import java.util.List;
import java.util.Optional;

public class CapsaleraEdi {

    private final ComandaMissatgeEDI raw;

    public CapsaleraEdi(ComandaMissatgeEDI raw) {
        this.raw = raw;
    }

    public CaWrapper ca() {
        return new CaWrapper(raw.getCA());
    }

    public Optional<CbWrapper> cb() {
        return Optional.ofNullable(raw.getCB()).map(CbWrapper::new);
    }

    public Optional<CcWrapper> cc() {
        return Optional.ofNullable(raw.getCC()).map(CcWrapper::new);
    }

    public Optional<CdWrapper> cd() {
        return Optional.ofNullable(raw.getCD()).map(CdWrapper::new);
    }

    public CiWrapper ci() {
        return new CiWrapper(raw.getCI());
    }

    public Optional<CpWrapper> cp() {
        return Optional.ofNullable(raw.getCP()).map(CpWrapper::new);
    }

    public List<CtWrapper> ct() {
        var ct = raw.getTextosLibres();
        if (ct == null) return List.of();
        return ct.stream().map(CtWrapper::new).toList();
    }
}
