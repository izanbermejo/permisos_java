package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.MagatzemNoSII;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.consum.SortidaPlataformaRepository;
import ames.comercial.albarans.internal.services.ComprovarMagatzemSII;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Retorna, per a una línia d'albarà de consum, els albarans de traspàs a plataforma dels quals s'ha
 * descomptat el pendent de consumir (els segments FIFO registrats a {@code albarans.sortides_plataforma}),
 * amb el codi d'albarà de traspàs i la quantitat consumida.
 * <p>
 * Només hi ha traçabilitat als magatzems marcats per al SII; a la resta es llença {@link MagatzemNoSII}
 * en comptes de retornar una llista buida que el frontend no sabria interpretar. El magatzem és el de la
 * capçalera de l'albarà, que en un consum és la plataforma d'on surt la mercaderia.
 */
@Service
public class ObtenirTraspassosConsumits {

    @Autowired SortidaPlataformaRepository sortidaPlataformaRepository;
    @Autowired AlbaraRepository albaraRepository;
    @Autowired ComprovarMagatzemSII comprovarMagatzemSII;

    public List<TraspasConsumitResponse> executar(KeyLiniaAlbara liniaConsum) {
        var idAlbara = liniaConsum.idAlbara();
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        if (!comprovarMagatzemSII.executar(albara.magatzem())) {
            throw new MagatzemNoSII(albara.magatzem());
        }
        return sortidaPlataformaRepository.findByLiniaConsum(liniaConsum).stream()
                .map(s -> (TraspasConsumitResponse) TraspasConsumitResponseImpl.builder()
                        .empresa(s.albaraTraspas().idAlbara().empresa())
                        .codiAlbara(s.albaraTraspas().idAlbara().codi())
                        .linia(s.albaraTraspas().linia())
                        .quantitat(s.quantitat())
                        .build())
                .toList();
    }

    @JsonDeserialize(builder = TraspasConsumitResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface TraspasConsumitResponse {
        String empresa();
        long codiAlbara();
        long linia();
        long quantitat();
    }

}
