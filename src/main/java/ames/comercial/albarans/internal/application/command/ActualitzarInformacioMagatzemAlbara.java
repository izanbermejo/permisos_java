package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActualitzarInformacioMagatzemAlbara {

    @Autowired AlbaraRepository albaraRepository;

    public void executar(KeyAlbara clauAlbara) {
        var albara = albaraRepository.find(clauAlbara).orElseThrow(() -> new AlbaraNoExisteix(clauAlbara));
    }

    @JsonDeserialize(builder = ActualitzarInformacioMagatzemAlbaraRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ActualitzarInformacioMagatzemAlbaraRequest {
        boolean isEnServei();
        boolean isEnPreparacio();
        boolean isServit();
    }

}
