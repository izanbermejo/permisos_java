package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.AlbaraTancat;
import ames.comercial.albarans.AlbaransException.ConsumMultiplesClients;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.services.AfegirLiniesConsum;
import ames.comercial.albarans.internal.services.AfegirLiniesConsum.PecaConsum;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Afegeix una línia de consum a un albarà de consum existent i obert. Valida que l'albarà no estigui
 * tancat ni facturat i que la peça sigui del mateix client de l'albarà (un consum és d'un únic client);
 * després delega a {@link AfegirLiniesConsum}.
 */
@Service
public class AfegirLiniaConsum {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired AfegirLiniesConsum afegirLiniesConsum;

    @Transactional
    public void executar(KeyAlbara idAlbara, PecaAfegirConsumRequest peca) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        // No es poden modificar les línies d'un albarà facturat ni amb moviments de magatzem actius
        albara.checkPotModificarLinies();
        // No es poden afegir línies a un albarà tancat (cal reobrir-lo)
        if (albara.isTancat()) {
            throw new AlbaraTancat(idAlbara);
        }
        // Un consum és sempre d'un únic client: la peça ha de ser del client de l'albarà
        if (!peca.articleClient().clicod().equals(albara.client().orElse(null))) {
            throw new ConsumMultiplesClients();
        }

        afegirLiniesConsum.executar(albara,
                List.of(new PecaConsum(peca.articleClient(), peca.quantitat(), peca.identificadorConsum())));
    }

    @JsonDeserialize(builder = PecaAfegirConsumRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PecaAfegirConsumRequest {
        KeyArticleClient articleClient();
        long quantitat();
        Optional<String> identificadorConsum();
    }

}
