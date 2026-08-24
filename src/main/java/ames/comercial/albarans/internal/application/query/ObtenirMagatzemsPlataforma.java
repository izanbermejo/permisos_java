package ames.comercial.albarans.internal.application.query;

import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.magatzem.internal.domain.Magatzem;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Llista els magatzems plataforma (Advantage {@code mag.tipus = 'P'}) per poder-hi fer consums.
 */
@Service
public class ObtenirMagatzemsPlataforma {

    @Autowired IObtenirMagatzems obtenirMagatzemAds;

    public List<PlataformaResponse> executar() {
        return obtenirMagatzemAds.all().stream()
                .filter(Magatzem::isPlataforma)
                .map(m -> (PlataformaResponse) PlataformaResponseImpl.builder()
                        .codi(m.codi())
                        .descripcio(m.descripcio())
                        .build())
                .toList();
    }

    @JsonDeserialize(builder = PlataformaResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PlataformaResponse {
        String codi();
        String descripcio();
    }

}
