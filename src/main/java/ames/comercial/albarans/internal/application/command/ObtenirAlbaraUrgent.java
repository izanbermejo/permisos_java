package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.AlbaraImpl;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObtenirAlbaraUrgent {

    @Autowired AlbaraRepository albaraRepository;

    public Albara executar(KeyAlbara id) {

        var albaraEntity = albaraRepository.find(id).orElseThrow(() -> new AlbaransException.AlbaraNoExisteix(id));

        return AlbaraImpl.builder()
                .from(albaraEntity)
                .build();
    }
}
