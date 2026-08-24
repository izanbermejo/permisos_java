package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.AlbaransException;
import ames.comercial.albarans.internal.domain.albara.CostEnviamentExpress;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MarcarAlbaraUrgent {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;

    @Transactional
    public void executar(KeyAlbara id, CostEnviamentExpress costEnviamentExpress, boolean isUrgent) {

        /*if (costEnviamentExpress == null || costEnviamentExpress.imp() == null
                || costEnviamentExpress.imp().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AlbaransException.ImportUrgentInvalid(albara);
        }*/

        var albaraEntity = albaraRepository.find(id).orElseThrow(() -> new AlbaransException.AlbaraNoExisteix(id));

        var totesLesLinies = liniaAlbaraRepository.findByAlbara(id);
        for (var linia : totesLesLinies) {
            var liniaActualitzada = linia.marcarLiniaUrgentNoUrgent(true);
            liniaAlbaraRepository.save(liniaActualitzada);
        }

        albaraEntity = albaraEntity.marcarUrgentNoUrgent(Optional.ofNullable(costEnviamentExpress), true);

        albaraRepository.save(albaraEntity);
    }
}
