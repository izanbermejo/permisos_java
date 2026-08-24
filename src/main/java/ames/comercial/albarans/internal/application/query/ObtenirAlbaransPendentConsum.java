package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.AlbaransException.MagatzemNoSII;
import ames.comercial.albarans.internal.services.ComprovarMagatzemSII;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Llista dels albarans de traspàs a plataforma amb pendent de consumir per a una peça en un magatzem
 * plataforma, ordenats per criteri FIFO. Reutilitza {@link ObtenirTraspassosPlataformaPendentConsumir}
 * i en mapeja el resultat a una resposta lleugera per al frontend.
 * <p>
 * Només té sentit als magatzems marcats per al SII, que són els únics on les línies de traspàs porten
 * pendent de consumir; a la resta es llença {@link MagatzemNoSII} en comptes de retornar una llista
 * buida que el frontend no sabria interpretar.
 */
@Service
public class ObtenirAlbaransPendentConsum {

    @Autowired ObtenirTraspassosPlataformaPendentConsumir obtenirTraspassosPendent;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;

    public List<AlbaraPendentConsumResponse> executar(String magatzemPlataforma, String empresa, KeyArticleClient articleClient) {
        if (!comprovarMagatzemSII.executar(magatzemPlataforma)) {
            throw new MagatzemNoSII(magatzemPlataforma);
        }
        return obtenirTraspassosPendent.executar(magatzemPlataforma, empresa, articleClient).stream()
                .map(ltp -> (AlbaraPendentConsumResponse) AlbaraPendentConsumResponseImpl.builder()
                        .empresa(ltp.linia().id().idAlbara().empresa())
                        .codiAlbara(ltp.linia().id().idAlbara().codi())
                        .linia(ltp.linia().id().linia())
                        .data(ltp.dataTraspas())
                        .quantitat(ltp.linia().quantitat())
                        .pendentConsumir(ltp.linia().quantitatPendentConsumir())
                        .build())
                .toList();
    }

    @JsonDeserialize(builder = AlbaraPendentConsumResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AlbaraPendentConsumResponse {
        String empresa();
        long codiAlbara();
        long linia();
        LocalDate data();
        /** Quantitat total de la línia de traspàs */
        long quantitat();
        /** Quantitat pendent de consumir de la línia de traspàs */
        long pendentConsumir();
    }

}
