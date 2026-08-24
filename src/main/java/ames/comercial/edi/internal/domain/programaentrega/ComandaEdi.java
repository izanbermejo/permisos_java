package ames.comercial.edi.internal.domain.programaentrega;

import ames.comercial.edi.beans.LineaPedidoPrevio;
import ames.comercial.edi.beans.Linea;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ComandaEdi {

    private final Linea raw;

    public ComandaEdi(Linea raw) {
        this.raw = raw;
    }

    public LaWrapper la() {
        return new LaWrapper(raw.getLA());
    }

    public Optional<LbWrapper> lb() {
        return Optional.ofNullable(raw.getLB()).map(LbWrapper::new);
    }

    public LcWrapper lc() {
        return new LcWrapper(raw.getLC());
    }

    public Optional<LdWrapper> ld() {
        return Optional.ofNullable(raw.getLD()).map(LdWrapper::new);
    }

    public Optional<LgWrapper> lg() {
        return Optional.ofNullable(raw.getLG()).map(LgWrapper::new);
    }

    public Optional<LsWrapper> ls() { return Optional.ofNullable(raw.getLS()).map(LsWrapper::new); }

    public Optional<LqWrapper> lq() {
        var lpp = raw.getLineasPedidoPrevio();
        if (lpp == null || lpp.isEmpty()) return Optional.empty();
        return Optional.ofNullable(lpp.get(0).getLQ()).map(LqWrapper::new);
    }

    public List<LqWrapper> lqs() {
        var lpp = raw.getLineasPedidoPrevio();
        if (lpp == null) return List.of();
        return lpp.stream()
                .map(LineaPedidoPrevio::getLQ)
                .filter(Objects::nonNull)
                .map(LqWrapper::new)
                .toList();
    }

    public Optional<LtWrapper> lt() {
        var obs = raw.getObservaciones();
        if (obs == null || obs.isEmpty()) return Optional.empty();
        return Optional.of(new LtWrapper(obs.get(0)));
    }

    public List<LeWrapper> le() {
        var emp = raw.getEmpaquetamientos();
        if (emp == null) return List.of();
        return emp.stream().map(LeWrapper::new).toList();
    }

    public List<LiniaEdi> linies() {
        var det = raw.getDetalles();
        if (det == null) return List.of();
        return det.stream().map(LiniaEdi::new).toList();
    }

    public List<AaWrapper> aa() {
        var alb = raw.getAlbaranesPrevios();
        if (alb == null) return List.of();
        return alb.stream()
                .filter(a -> a.getAA() != null)
                .map(a -> new AaWrapper(a.getAA()))
                .toList();
    }
}
