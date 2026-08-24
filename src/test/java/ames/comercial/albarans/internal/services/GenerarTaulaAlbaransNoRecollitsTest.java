package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransServitsNoRecollits.AlbaraNoRecollit;
import ames.comercial.albarans.internal.domain.albara.InformacioMagatzem;
import ames.comercial.albarans.internal.domain.albara.InformacioMagatzemImpl;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.services.GenerarTaulaAlbaransNoRecollits.Generador;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.InformacioEnviamentImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerarTaulaAlbaransNoRecollitsTest {

    private final Generador generador = new Generador(
            Map.of("0001", "0001 - Domicili del client"),
            Map.of("25", "25 - TRANSPORTS INTERNACIONALS DE CATALUNYA"));

    @Test
    void albaraAmbTotesLesDadesInformades() {
        var html = generador.generar(List.of(albara(1234L, "10", Optional.of(500L), Optional.of(3L))), false);

        // Empresa i codi de l'albarà amb els zeros al davant
        assertTrue(html.contains(">10/0001234</td>"), html);
        assertTrue(html.contains(">12/08/2026<"), html);
        assertTrue(html.contains(">100234 - ACME EUROPE<"), html);
        // Forma d'enviament, incoterm i destí en el format del frontend, escurçats a 34 caràcters
        assertTrue(html.contains(">Carretera • DAP • 0001 - Domicili…<"), html);
        // Transportista escurçat a 24 caràcters
        assertTrue(html.contains(">25 - TRANSPORTS INTERNA…<"), html);
        assertTrue(html.contains(">3</td>"), html);
        assertTrue(html.contains(">500</td>"), html);
    }

    @Test
    void albaraSenseClientNiTransportistaNiPesos() {
        var albara = new AlbaraNoRecollit(
                KeyAlbara.of(9L, "20"),
                LocalDate.of(2026, 8, 12),
                "0002",
                Optional.empty(),
                Optional.empty(),
                InformacioEnviamentImpl.builder()
                        .formaEnviament(FormaEnviament.DESCONEGUT)
                        .incoterm(Incoterm.EXW)
                        .desti("")
                        .build(),
                informacioMagatzem(Optional.empty(), Optional.empty()),
                0L,
                "AMES",
                Optional.empty());

        var html = generador.generar(List.of(albara), false);

        // El client, el transportista i els pesos no informats es marquen amb un guió
        assertEquals(4, html.split("—", -1).length - 1, html);
    }

    @Test
    void elsNomsDeLesColumnesSurtenEnCatalaIEnAngles() {
        var html = generador.generar(List.of(albara(1L, "10", Optional.of(1L), Optional.of(1L))), true);

        assertTrue(html.contains("Albarà<br><span style=\"font-weight: normal; color: #495057;\">Delivery note</span>"),
                html);
        for (var nom : List.of("Data", "Date", "Client", "Customer", "Forma enviament", "Shipping method",
                "Transportista", "Carrier", "Bultos", "Packages", "Pes brut", "Gross weight",
                "Creat per", "Created by")) {
            assertTrue(html.contains(">" + nom + "<"), nom + " no surt a la capçalera: " + html);
        }
    }

    @Test
    void elCosDelCorreuPortaElMissatgeEnCatalaIEnAngles() {
        var cos = generador.generarCorreu("Missatge en català.", "Message in English.",
                List.of(albara(1L, "10", Optional.of(1L), Optional.of(1L))), false);

        // Un darrere de l'altre i abans de la taula
        assertTrue(cos.indexOf("Missatge en català.") < cos.indexOf("Message in English."), cos);
        assertTrue(cos.indexOf("Message in English.") < cos.indexOf("<table"), cos);
        // El peu també és a les dues llengües
        assertTrue(cos.contains("no cal respondre'l."), cos);
        assertTrue(cos.contains("there is no need to reply."), cos);
    }

    @Test
    void nomesLavisDelMagatzemPortaQuiHaCreatLalbara() {
        var albarans = List.of(albara(1L, "10", Optional.of(1L), Optional.of(1L)));

        var htmlComercial = generador.generar(albarans, false);
        assertFalse(htmlComercial.contains("Creat per"), htmlComercial);
        assertFalse(htmlComercial.contains("Marta Puig"), htmlComercial);

        var htmlMagatzem = generador.generar(albarans, true);
        assertTrue(htmlMagatzem.contains("Creat per"), htmlMagatzem);
        assertTrue(htmlMagatzem.contains(">Marta Puig<"), htmlMagatzem);
    }

    @Test
    void elsNomsAmbCaractersHtmlSescapen() {
        var albara = new AlbaraNoRecollit(
                KeyAlbara.of(1L, "10"),
                LocalDate.of(2026, 8, 12),
                "0001",
                Optional.of("100234"),
                Optional.of("SMITH & <SON>"),
                informacioEnviament(),
                informacioMagatzem(Optional.empty(), Optional.empty()),
                77L,
                "Marta Puig",
                Optional.of("marta.puig@ames.group"));

        var html = generador.generar(List.of(albara), false);

        assertTrue(html.contains("100234 - SMITH &amp; &lt;SON&gt;"), html);
    }

    private static AlbaraNoRecollit albara(long codi, String empresa, Optional<Long> pesBrut, Optional<Long> bultos) {
        return new AlbaraNoRecollit(
                KeyAlbara.of(codi, empresa),
                LocalDate.of(2026, 8, 12),
                "0001",
                Optional.of("100234"),
                Optional.of("ACME EUROPE"),
                informacioEnviament(),
                informacioMagatzem(pesBrut, bultos),
                77L,
                "Marta Puig",
                Optional.of("marta.puig@ames.group"));
    }

    private static InformacioEnviament informacioEnviament() {
        return InformacioEnviamentImpl.builder()
                .formaEnviament(FormaEnviament.CAMIO)
                .incoterm(Incoterm.DAP)
                .desti("0001")
                .transportista("25")
                .build();
    }

    private static InformacioMagatzem informacioMagatzem(Optional<Long> pesBrut, Optional<Long> bultos) {
        return InformacioMagatzemImpl.builder()
                .isEnPreparacio(true)
                .isEnServei(true)
                .isServit(true)
                .isEntregat(false)
                .isAvisEnviat(false)
                .pesBrut(pesBrut)
                .bultos(bultos)
                .build();
    }

}
