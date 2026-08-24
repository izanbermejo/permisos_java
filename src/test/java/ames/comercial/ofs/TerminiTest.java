package ames.comercial.ofs;

import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.domain.TerminiImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TerminiTest {

    static Termini termini(int quantitatEsperada, int quantitatRebuda) {
        return TerminiImpl.builder()
                .data(LocalDate.now())
                .quantitat(quantitatEsperada)
                .quantitatRebuda(quantitatRebuda)
                .build();
    }

    @Test
    @DisplayName("Es calcula correctament la quantitat pendent quan es sobrepessa")
    public void calculPendentSobrepassa() {
        var termini = termini(200, 50).aplicaEntrada(300);
        assertEquals(termini.quantitatPendent(), 0);
    }

    @Test
    @DisplayName("Es calcula correctament la quantitat pendent quan no es sobrepessa")
    public void calculPendentNoSobrepassa() {
        var termini = termini(200, 50).aplicaEntrada(50);
        assertEquals(termini.quantitatPendent(), 100);
    }

    @Test
    @DisplayName("Es calcula correctament la quantitat rebuda quan es sobrepessa")
    public void calculRebutSobrepassa() {
        var termini = termini(200, 50).aplicaEntrada(300);
        assertEquals(termini.quantitatRebuda(), 200);
    }

    @Test
    @DisplayName("Es calcula correctament la quantitat rebuda quan no es sobrepessa")
    public void calculRebutNoSobrepassa() {
        var termini = termini(200, 50).aplicaEntrada(50);
        assertEquals(termini.quantitatRebuda(), 100);
    }

}
