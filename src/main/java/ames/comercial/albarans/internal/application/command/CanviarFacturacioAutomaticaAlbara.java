package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraFacturatNoCanviarAutofacturable;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Canvia el flag d'autofacturable d'un albarà, que fins ara només es decidia en el moment de crear-lo
 * a partir del client i de les línies ({@link ames.comercial.albarans.internal.services.CalcularAutofacturableAlbara}).
 * <p>
 * Només es permet en albarans de client i mentre no s'hagi començat a facturar: ni el flag de
 * capçalera (validat al domini a {@link ames.comercial.albarans.internal.domain.albara.Albara#checkPotCanviarFacturacioAutomatica})
 * ni cap línia amb quantitat ja facturada. El canvi es replica a l'Advantage (albcap.factauto) perquè
 * és el camp que llegeix el procés de facturació automàtica.
 */
@Service
public class CanviarFacturacioAutomaticaAlbara {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;

    @Transactional
    public void executar(KeyAlbara idAlbara, CanviarFacturacioAutomaticaAlbaraRequest req) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        albara.checkPotCanviarFacturacioAutomatica();
        if (liniaAlbaraRepository.isHiHaLiniesFacturades(idAlbara)) {
            throw new AlbaraFacturatNoCanviarAutofacturable(idAlbara);
        }
        var albaraCanviat = albara.canviarFacturacioAutomatica(req.isFacturacioAutomatica());
        albaraRepository.save(albaraCanviat);
        ReplicaAdvantage.instance().addUpdateFacturacioAutomaticaAlbara(albaraCanviat);
    }

    @JsonDeserialize(builder = CanviarFacturacioAutomaticaAlbaraRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CanviarFacturacioAutomaticaAlbaraRequest {
        boolean isFacturacioAutomatica();
    }

}
