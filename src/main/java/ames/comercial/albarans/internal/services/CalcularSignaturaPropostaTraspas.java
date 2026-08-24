package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Calcula una signatura estable (SHA-256) de l'estat rellevant per a la proposta de creació d'albarans de traspàs:
 * les peces a servir i els albarans de traspàs oberts candidats a ser aprofitats.
 * <p>
 * Es genera al previsualitzar i es torna a calcular al confirmar; si difereix, l'estat ha canviat entre les dues
 * fases (algú ha servit peces, ha aparegut o s'ha tancat un albarà de traspàs obert, etc.) i la creació s'avorta.
 */
@Component
public class CalcularSignaturaPropostaTraspas {

    public String executar(LocalDate dataObjectiu, Map<KeyArticleClient, Long> quantitats, List<AlbaraTraspasAmbLiniesDTO> albaransOberts) {
        var sb = new StringBuilder();

        sb.append("==DATA==\n").append(dataObjectiu).append('\n');

        sb.append("==PECES==\n");
        quantitats.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<KeyArticleClient, Long> e) -> e.getKey().artint())
                        .thenComparing(e -> e.getKey().clicod()))
                .forEach(e -> sb
                        .append(e.getKey().artint()).append('#').append(e.getKey().clicod())
                        .append('|').append(e.getValue())
                        .append('\n'));

        sb.append("==ALBARANS_TRASPAS_OBERTS==\n");
        albaransOberts.stream()
                .sorted(Comparator
                        .comparing((AlbaraTraspasAmbLiniesDTO a) -> a.idAlbara().empresa())
                        .thenComparingLong(a -> a.idAlbara().codi()))
                .forEach(a -> {
                    sb.append(a.idAlbara().empresa()).append('#').append(a.idAlbara().codi())
                            .append('|').append(a.data())
                            .append('|').append(a.empresaReceptora())
                            .append('|').append(a.magatzemReceptor())
                            .append('\n');
                    a.linies().stream()
                            .sorted(Comparator.comparingLong(ln -> ln.idLiniaAlbara().linia()))
                            .forEach(ln -> sb
                                    .append("  L|").append(ln.idLiniaAlbara().linia())
                                    .append('|').append(ln.articleClient().artint()).append('#').append(ln.articleClient().clicod())
                                    .append('|').append(ln.codiPartidaArantzelaria())
                                    .append('|').append(ln.quantitat())
                                    .append('\n'));
                });

        return sha256(sb.toString());
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
            throw new AppException("No s'ha pogut calcular la signatura de la proposta de traspàs", e);
        }
    }

}
