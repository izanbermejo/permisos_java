package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;
import ames.comercial.server.exception.AppException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Calcula una signatura estable (SHA-256) de l'estat rellevant per a la proposta de creació d'albarans:
 * les línies de comanda a servir i els albarans oberts candidats a ser aprofitats.
 * <p>
 * La signatura es genera al moment de previsualitzar la proposta i es torna a calcular en el moment de
 * confirmar-la. Si difereix, vol dir que alguna cosa ha canviat entre la previsualització i la confirmació
 * (una altra persona ha servit línies, ha canviat un preu, ha aparegut o s'ha tancat un albarà obert, etc.)
 * i la creació s'ha d'abortar per evitar crear una cosa diferent de la que l'usuari va acceptar.
 */
@Component
public class CalcularSignaturaProposta {

    public String executar(LocalDate dataObjectiu, List<InformacioLiniaComandaDTO> linies, List<AlbaraAmbLiniesDTO> albaransOberts) {
        var sb = new StringBuilder();

        sb.append("==DATA==\n").append(dataObjectiu).append('\n');

        sb.append("==LINIES==\n");
        linies.stream()
                .sorted(Comparator
                        .comparingLong((InformacioLiniaComandaDTO l) -> l.id().comanda())
                        .thenComparingLong(l -> l.id().numero()))
                .forEach(l -> sb
                        .append(l.id().comanda()).append('#').append(l.id().numero())
                        .append('|').append(l.articleClient().artint()).append('#').append(l.articleClient().clicod())
                        .append('|').append(l.quantitat())
                        .append('|').append(l.quantitatServida())
                        .append('|').append(l.quantitatReservada())
                        .append('|').append(preu(l.preu().valor())).append('#').append(l.preu().divisa())
                        .append('|').append(l.isPreuFixat())
                        .append('|').append(l.comandaSegonsClient())
                        // Adreça i informació d'enviament determinen l'agrupació d'albarans
                        .append('|').append(l.adresa())
                        .append('|').append(l.informacioEnviament())
                        .append('\n'));

        sb.append("==ALBARANS_OBERTS==\n");
        albaransOberts.stream()
                .sorted(Comparator
                        .comparing((AlbaraAmbLiniesDTO a) -> a.idAlbara().empresa())
                        .thenComparingLong(a -> a.idAlbara().codi()))
                .forEach(a -> {
                    sb.append(a.idAlbara().empresa()).append('#').append(a.idAlbara().codi())
                            .append('|').append(a.data())
                            .append('|').append(a.adresa())
                            .append('|').append(a.informacioEnviament())
                            .append('\n');
                    a.linies().stream()
                            .sorted(Comparator.comparingLong(ln -> ln.idLiniaAlbara().linia()))
                            .forEach(ln -> sb
                                    .append("  L|").append(ln.idLiniaAlbara().linia())
                                    .append('|').append(ln.articleClient().artint()).append('#').append(ln.articleClient().clicod())
                                    .append('|').append(ln.quantitat())
                                    .append('|').append(preu(ln.preu().valor())).append('#').append(ln.preu().divisa())
                                    .append('\n'));
                });

        return sha256(sb.toString());
    }

    private String preu(BigDecimal valor) {
        return valor == null ? "0" : valor.stripTrailingZeros().toPlainString();
    }

    private String sha256(String contingut) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contingut.getBytes(StandardCharsets.UTF_8));
            var hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("No s'ha pogut calcular la signatura de la proposta", e);
        }
    }

}
