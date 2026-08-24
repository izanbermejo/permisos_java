package ames.comercial.ofs;

import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.domain.TerminiImpl;
import ames.comercial.ofs.internal.domain.service.AplicarEntradaTermini;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AplicarEntradaTerminiTest {

    static Termini termini  = TerminiImpl.builder()
                .data(LocalDate.now())
                .quantitat(200)
                .quantitatRebuda(100)
                .build();

    @Test
    @DisplayName("Es calcula correctament l'excés de l'entrada quan es sobrepassa")
    public void calculExcesSobrepassa() {
        // Termini de 200 peces amb ja 100 rebudes
        var resp = new AplicarEntradaTermini(termini).executar(300);
        assertEquals(resp.quantitatExces(), 200);
    }

    @Test
    @DisplayName("Es calcula correctament l'excés de l'entrada quan no arriba")
    public void calculExcesNoArriba() {
        // Termini de 200 peces amb ja 100 rebudes amb entrada de 50
        var resp = new AplicarEntradaTermini(termini).executar(50);
        assertEquals(resp.quantitatExces(), 0);
    }

}
