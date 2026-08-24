package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CanviarFacturatAlbara {

    @Autowired AlbaraRepository albaraRepository;

    void executar (KeyAlbara clauAlbara, boolean isFacturat) {
        var albara = albaraRepository.find(clauAlbara).orElseThrow();
        var albaraCanviada = albara.canviarFacturat(isFacturat);
        albaraRepository.save(albaraCanviada);
        ReplicaAdvantage.instance().addUpdateFacturatAlbara(albaraCanviada);
    }

}
