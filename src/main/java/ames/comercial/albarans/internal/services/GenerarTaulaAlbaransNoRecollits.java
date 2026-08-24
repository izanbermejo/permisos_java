package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.IObtenirDestinsTransport;
import ames.comercial.advantage.IObtenirTransportistesAds;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransServitsNoRecollits.AlbaraNoRecollit;
import ames.comercial.server.I18N;
import ames.comercial.server.Languages;
import ames.comercial.shared.InformacioEnviament;
import com.google.common.base.Strings;
import com.google.common.html.HtmlEscapers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

/**
 * Munta la taula HTML dels albarans servits i no recollits que s'envia per correu, tant a cada persona
 * com a cada magatzem: l'única diferència entre les dues és la columna amb el nom i els cognoms de qui
 * va crear l'albarà, que només surt a l'avís del magatzem.
 * <p>
 * Els mestres de destins i de transportistes viuen a l'Advantage i es llegeixen sencers un sol cop
 * (veure {@link #preparar()}), perquè l'avís es munta un correu per destinatari i consultar-los per cada
 * fila voldria dir centenars de consultes a l'Advantage per enviament.
 */
@Service
public class GenerarTaulaAlbaransNoRecollits {

    @Autowired IObtenirDestinsTransport obtenirDestinsTransport;
    @Autowired IObtenirTransportistesAds obtenirTransportistesAds;

    /**
     * Llegeix els mestres de l'Advantage i retorna un generador que els reaprofita per a totes les
     * taules d'un mateix enviament.
     */
    public Generador preparar() {
        // Els codis del mestre poden estar repetits, per això es queda el primer que arriba
        var destins = obtenirDestinsTransport.all().stream()
                .collect(toMap(d -> d.codi(), d -> etiqueta(d.codi(), d.nom()), (a, b) -> a));
        var transportistes = obtenirTransportistesAds.all().stream()
                .collect(toMap(t -> t.codi(), t -> etiqueta(t.codi(), t.descripcio()), (a, b) -> a));
        return new Generador(destins, transportistes);
    }

    private static String etiqueta(String codi, String nom) {
        var nomNet = Strings.nullToEmpty(nom).trim();
        return nomNet.isEmpty() ? codi.trim() : codi.trim() + " - " + nomNet;
    }

    public static class Generador {

        /**
         * Amplades màximes en caràcters de les columnes de text lliure. El que sobra es talla i es marca
         * amb punts suspensius: sense límit, un nom de client o de transportista llarg faria una taula
         * massa ampla per a la finestra del client de correu.
         */
        private static final int MAX_CHARS_CLIENT = 25;
        private static final int MAX_CHARS_ENVIAMENT = 34;
        private static final int MAX_CHARS_TRANSPORTISTA = 24;
        private static final int MAX_CHARS_CREADOR = 24;

        private static final String SENSE_VALOR = "—";

        /**
         * Idioma amb què es tradueixen les formes d'enviament i es formaten els números. Es fixa i no es
         * llegeix del request perquè el text dels avisos no depèn de qui el rebi i perquè la tasca que els
         * envia s'executa sense request i, per tant, sense idioma.
         */
        private static final Locale IDIOMA = Languages.getDefault();

        private static final DateTimeFormatter FORMAT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private static final String ESTIL_TAULA =
                "border-collapse: collapse; font-family: Arial, Helvetica, sans-serif; font-size: 12px;";
        private static final String ESTIL_CAPSALERA =
                "background-color: #e9ecef; border: 1px solid #adb5bd; padding: 4px 8px; text-align: left; white-space: nowrap;";
        private static final String ESTIL_CAPSALERA_ANGLES = "font-weight: normal; color: #495057;";
        private static final String ESTIL_CELA =
                "border: 1px solid #dee2e6; padding: 3px 8px; white-space: nowrap;";
        private static final String ESTIL_CELA_NUMERICA = ESTIL_CELA + " text-align: right;";

        /** Codi del destí o del transportista -> "codi - nom" */
        private final Map<String, String> destins;
        private final Map<String, String> transportistes;
        private final NumberFormat formatNumeric;

        /** Visible dins del paquet per poder provar la taula sense passar per l'Advantage */
        Generador(Map<String, String> destins, Map<String, String> transportistes) {
            this.destins = destins;
            this.transportistes = transportistes;
            this.formatNumeric = NumberFormat.getNumberInstance(IDIOMA);
        }

        /**
         * Cos sencer del correu: la introducció que li passa cada avís en català i en anglès, la taula
         * d'albarans i el peu que avisa que el correu és automàtic.
         *
         * @param isMostrarCreacio si s'ha d'afegir la columna amb el nom i els cognoms de qui va crear
         *                         l'albarà (avís del magatzem)
         */
        public String generarCorreu(String introduccioCatala, String introduccioAngles,
                                    List<AlbaraNoRecollit> albarans, boolean isMostrarCreacio) {
            return """
                    <html><body style="font-family: Arial, Helvetica, sans-serif; font-size: 13px; color: #212529;">
                    <p>{introduccioCatala}</p>
                    <p>{introduccioAngles}</p>
                    {taula}
                    <p style="font-size: 11px; color: #6c757d;">Aquest correu s'envia automàticament; no cal respondre'l.<br>
                    This email is sent automatically; there is no need to reply.</p>
                    </body></html>
                    """
                    .replace("{introduccioCatala}", escapar(introduccioCatala))
                    .replace("{introduccioAngles}", escapar(introduccioAngles))
                    .replace("{taula}", generar(albarans, isMostrarCreacio));
        }

        /**
         * @param isMostrarCreacio si s'ha d'afegir la columna amb el nom i els cognoms de qui va crear
         *                         l'albarà (avís del magatzem)
         */
        public String generar(List<AlbaraNoRecollit> albarans, boolean isMostrarCreacio) {
            var html = new StringBuilder();
            html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"").append(ESTIL_TAULA).append("\">\n");

            html.append("<tr>");
            capsalera(html, "Albarà", "Delivery note");
            capsalera(html, "Data", "Date");
            capsalera(html, "Client", "Customer");
            capsalera(html, "Forma enviament", "Shipping method");
            capsalera(html, "Transportista", "Carrier");
            capsalera(html, "Bultos", "Packages");
            capsalera(html, "Pes brut", "Gross weight");
            if (isMostrarCreacio) {
                capsalera(html, "Creat per", "Created by");
            }
            html.append("</tr>\n");

            for (var albara : albarans) {
                var informacioMagatzem = albara.informacioMagatzem();
                html.append("<tr>");
                cela(html, albara.id().empresa() + "/" + albara.id().codiFormat());
                cela(html, albara.data().format(FORMAT_DATA));
                cela(html, escurcar(client(albara), MAX_CHARS_CLIENT));
                cela(html, escurcar(formaEnviament(albara.informacioEnviament()), MAX_CHARS_ENVIAMENT));
                cela(html, escurcar(transportista(albara.informacioEnviament()), MAX_CHARS_TRANSPORTISTA));
                celaNumerica(html, informacioMagatzem.bultos());
                celaNumerica(html, informacioMagatzem.pesBrut());
                if (isMostrarCreacio) {
                    cela(html, escurcar(albara.nomCreacio(), MAX_CHARS_CREADOR));
                }
                html.append("</tr>\n");
            }

            html.append("</table>");
            return html.toString();
        }

        /**
         * Nom de la columna en català i, a sota, en anglès. Les dues llengües van en línies separades i no
         * amb una barra pel mig perquè si no la capçalera quedaria més ampla que el contingut, que és
         * justament el que els límits de caràcters de les cel·les volen evitar.
         */
        private static void capsalera(StringBuilder html, String catala, String angles) {
            html.append("<th style=\"").append(ESTIL_CAPSALERA).append("\">")
                    .append(escapar(catala))
                    .append("<br><span style=\"").append(ESTIL_CAPSALERA_ANGLES).append("\">")
                    .append(escapar(angles))
                    .append("</span></th>");
        }

        private static void cela(StringBuilder html, String text) {
            html.append("<td style=\"").append(ESTIL_CELA).append("\">").append(escapar(text)).append("</td>");
        }

        private void celaNumerica(StringBuilder html, Optional<Long> valor) {
            var text = valor.map(v -> formatNumeric.format(v)).orElse(SENSE_VALOR);
            html.append("<td style=\"").append(ESTIL_CELA_NUMERICA).append("\">").append(escapar(text)).append("</td>");
        }

        /** Codi i nom del client. Els albarans de traspàs i de consum no en tenen. */
        private static String client(AlbaraNoRecollit albara) {
            return albara.client()
                    .map(codi -> etiqueta(codi, albara.clientNom().orElse("")))
                    .orElse(SENSE_VALOR);
        }

        /** Forma d'enviament, incoterm i destí en el mateix format que es mostra al frontend. */
        private String formaEnviament(InformacioEnviament informacioEnviament) {
            var desti = destins.getOrDefault(informacioEnviament.desti(), informacioEnviament.desti().trim());
            return I18N.getLiteral(IDIOMA, informacioEnviament.formaEnviament().toString())
                    + " • " + informacioEnviament.incoterm()
                    + " • " + desti;
        }

        private String transportista(InformacioEnviament informacioEnviament) {
            return informacioEnviament.transportista()
                    .filter(codi -> !codi.isBlank())
                    .map(codi -> transportistes.getOrDefault(codi, codi.trim()))
                    .orElse(SENSE_VALOR);
        }

        private static String escurcar(String text, int maxChars) {
            var net = Strings.nullToEmpty(text).trim();
            return net.length() <= maxChars ? net : net.substring(0, maxChars - 1) + "…";
        }

        private static String escapar(String text) {
            return HtmlEscapers.htmlEscaper().escape(text);
        }

    }

}
