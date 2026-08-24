package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Canvia el número d'albarà especial d'un albarà. Només es permet mentre l'albarà està obert (validat
 * al domini a {@link ames.comercial.albarans.internal.domain.albara.Albara#canviarNumeroAlbaraEspecial}).
 * Un valor buit o en blanc esborra el número d'albarà especial.
 */
@Service
public class CanviarAlbaraEspecial {

    @Autowired AlbaraRepository albaraRepository;

    @Transactional
    public void executar(KeyAlbara idAlbara, CanviarAlbaraEspecialRequest req) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        var valor = req.numeroAlbaraEspecial()
                .map(String::trim)
                .filter(v -> !v.isEmpty());
        albaraRepository.save(albara.canviarNumeroAlbaraEspecial(valor));
    }

    @JsonDeserialize(builder = CanviarAlbaraEspecialRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CanviarAlbaraEspecialRequest {
        Optional<String> numeroAlbaraEspecial();
    }

}
