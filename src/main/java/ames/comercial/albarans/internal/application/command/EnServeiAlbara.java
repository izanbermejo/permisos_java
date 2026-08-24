package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnServeiAlbara {

    @Autowired AlbaraRepository albaraRepository;

    public void executar(KeyAlbara clauAlbara) {
        var albara = albaraRepository.find(clauAlbara).orElseThrow(() -> new AlbaraNoExisteix(clauAlbara));
        var nouAlbara = albara.enServei();
        albaraRepository.save(nouAlbara);
        ReplicaAdvantage.instance().addAlbaraDeleteInsert(nouAlbara);
    }

}
