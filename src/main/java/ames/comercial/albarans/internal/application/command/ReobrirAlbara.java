package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reobre un albarà tancat (isTancat = false). La regla de si es pot reobrir (ni facturat ni amb
 * moviments de magatzem actius) està encapsulada al domini: {@code Albara.reobrir()} →
 * {@code Albara.checkPotModificarLinies()}.
 */
@Service
public class ReobrirAlbara {

    @Autowired AlbaraRepository albaraRepository;

    @Transactional
    public void executar(KeyAlbara idAlbara) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        albaraRepository.save(albara.reobrir());
    }

}
