package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TancarAlbara {

    @Autowired AlbaraRepository albaraRepository;

    @Transactional
    public void executar(KeyAlbara idAlbara) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        albaraRepository.save(albara.tancar());
    }

}
