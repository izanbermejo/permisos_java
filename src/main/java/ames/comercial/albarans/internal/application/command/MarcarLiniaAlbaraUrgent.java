package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MarcarLiniaAlbaraUrgent {

    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired AlbaraRepository albaraRepository;

    @Transactional
    public void executar(KeyAlbara id, long idLiniaAlbara, boolean isUrgent) {

        var albaraEntity = albaraRepository.find(id).orElseThrow(() -> new AlbaransException.AlbaraNoExisteix(id));
        var linia = liniaAlbaraRepository.find(id, idLiniaAlbara).orElseThrow(
                () -> new AlbaransException.LiniaAlbaraNoExisteix(KeyLiniaAlbara.of(id, idLiniaAlbara)));

        if (!albaraEntity.isUrgent() && isUrgent) throw new AlbaransException.AlbaraUrgent(id);

        var liniaActualitzada = linia.marcarLiniaUrgentNoUrgent(isUrgent);

        liniaAlbaraRepository.save(liniaActualitzada);

        // Si es desmarca una linia i ja no en queda cap urgent, es desmarca l'albarà sencer
        if (!isUrgent) {
            var totesLesLinies = liniaAlbaraRepository.findByAlbara(id);
            boolean quedaAlgunaLiniaUrgent = totesLesLinies.stream().anyMatch(LiniaAlbara::isUrgent);

            if (!quedaAlgunaLiniaUrgent) {
                albaraEntity = albaraEntity.marcarUrgentNoUrgent(Optional.empty(), false);
                albaraRepository.save(albaraEntity);
            }
        }
    }
}
