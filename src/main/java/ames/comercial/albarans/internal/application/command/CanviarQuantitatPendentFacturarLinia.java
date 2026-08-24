package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.LiniaAlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.TraspasAbonableSensePendentFacturar;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rectifica manualment la quantitat pendent de facturar d'una línia d'albarà.
 * <p>
 * La nova quantitat no pot superar la quantitat de la línia (validat al domini a
 * {@link ames.comercial.albarans.internal.domain.linia.LiniaAlbara#canviarQuantitatPendentFacturar}).
 * El canvi es replica a l'Advantage i es recalcula el flag de facturat de la capçalera, perquè
 * l'albarà no quedi marcat com a facturat sense cap línia facturada ni a l'inrevés.
 * <p>
 * No s'admet en els traspassos abonables: allà les línies no es facturen mai i el pendent de facturar
 * ha de quedar-se a zero, que és la contrapartida del pendent d'abonar de {@code penabo}.
 */
@Service
public class CanviarQuantitatPendentFacturarLinia {

    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired AlbaraRepository albaraRepository;
    @Autowired CanviarFacturatAlbara canviarFacturatAlbara;

    @Transactional
    public void executar(KeyLiniaAlbara idLinia, CanviarQuantitatPendentFacturarLiniaRequest req) {
        var idAlbara = idLinia.idAlbara();
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        if (albara.isTraspasAbonable()) {
            throw new TraspasAbonableSensePendentFacturar(idAlbara);
        }
        var linia = liniaAlbaraRepository.find(idLinia).orElseThrow(() -> new LiniaAlbaraNoExisteix(idLinia));
        var liniaCanviada = linia.canviarQuantitatPendentFacturar(req.quantitatPendentFacturar());
        liniaAlbaraRepository.save(liniaCanviada);
        ReplicaAdvantage.instance().addLiniaAlbaraDeleteInsert(liniaCanviada);
        actualitzarFacturatAlbara(idLinia.idAlbara());
    }

    /** Marca l'albarà com a facturat si li queda alguna línia amb quantitat facturada, i a l'inrevés. */
    private void actualitzarFacturatAlbara(KeyAlbara idAlbara) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        var isFacturat = liniaAlbaraRepository.isHiHaLiniesFacturades(idAlbara);
        if (albara.isFacturat() != isFacturat) {
            canviarFacturatAlbara.executar(idAlbara, isFacturat);
        }
    }

    @JsonDeserialize(builder = CanviarQuantitatPendentFacturarLiniaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CanviarQuantitatPendentFacturarLiniaRequest {
        long quantitatPendentFacturar();
    }

}
