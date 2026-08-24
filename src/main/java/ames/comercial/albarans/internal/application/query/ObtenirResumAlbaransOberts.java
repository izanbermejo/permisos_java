package ames.comercial.albarans.internal.application.query;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.albarans.internal.application.query.ObtenirResumAlbaransOberts.ResumAlbaransObertsResponse.ResumAlbaraObert;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ObtenirResumAlbaransOberts {

    @Autowired IObtenirClientAds obtenirClientAds;

    public ResumAlbaransObertsResponse executar (String codiClient) {
        // Obtenció del client
        var client = obtenirClientAds.get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));


        return ResumAlbaransObertsResponseImpl.builder()
                .codiClient(client.clicod())
                .nomClient(client.nom())
                .isBloquejat(client.isImpagament())
                .albarans(obtenirAlbaransOberts(codiClient))
                .build();
    }

    private List<ResumAlbaraObert> obtenirAlbaransOberts(String codiClient) {
        return List.of();
    }

    @JsonDeserialize(builder = ResumAlbaransObertsResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ResumAlbaransObertsResponse {

        String codiClient();
        String nomClient();
        boolean isBloquejat();
        List<ResumAlbaraObert> albarans();

        @Derived default int numAlbarans() { return albarans().size(); }

        @JsonDeserialize(builder = ResumAlbaraObertImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ResumAlbaraObert {
            KeyAlbara id();
            String client();
            LocalDate data();
            String magatzem();
            Adresa adresa();
            InformacioEnviament informacioEnviament();
            String usuariCreacio();
        }

    }

}
