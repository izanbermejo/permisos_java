package ames.comercial.ofs.internal.domain.service;

import ames.comercial.ofs.internal.domain.Termini;

public class AplicarEntradaTermini {

    Termini termini;

    public AplicarEntradaTermini(Termini termini) {
        this.termini = termini;
    }

    public AplicarEntradaResponse executar (long quantitatEntrada) {
        // Càlcul de la quantitat que excedeix la línia. Com a mínim serà 0
        var quantitatExces = Math.max(0, quantitatEntrada - termini.quantitatPendent());
        return new AplicarEntradaResponse(termini.aplicaEntrada(quantitatEntrada), quantitatExces);
    }

    public record AplicarEntradaResponse(Termini termini, long quantitatExces) {}

}
