package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.TraspasAbonableFacturacioIniciada;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marca o desmarca un albarà de traspàs com a abonable quan ja existeix, amb línies o sense.
 * <p>
 * El flag no és només informatiu: decideix si la contrapartida del traspàs és una factura o un abonament,
 * i les dues s'exclouen. Per això el canvi arrossega les línies que ja hi hagi:
 * <ul>
 *   <li><b>Passa a abonable</b>: les línies deixen de tenir quantitat pendent de facturar i cadascuna
 *       genera el seu pendent d'abonar a {@code penabo}.</li>
 *   <li><b>Deixa de ser abonable</b>: es treuen els pendents d'abonar i les línies recuperen tota la
 *       quantitat com a pendent de facturar (el traspàs creua empreses, que és condició del flag).</li>
 * </ul>
 * Només es permet mentre no s'hagi començat a facturar ni abonar. El preu de les línies no es recalcula
 * encara que la tarifa que els correspondria canviï; si cal, l'usuari les pot refer.
 */
@Service
public class CanviarTraspasAbonable {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;

    @Transactional
    public void executar(KeyAlbara idAlbara, CanviarTraspasAbonableRequest request) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        // Si el flag no canvia no es fa res: s'evita reescriure línies i replicar-ho tot
        if (albara.isTraspasAbonable() == request.isTraspasAbonable()) {
            return;
        }
        // En les dues direccions, l'albarà no pot haver entrat en circulació: ni facturat ni amb moviment
        // de magatzem (en preparació, en servei, servit o entregat). Com que un traspàs no s'abona fins
        // que s'ha servit, això és el que garanteix que tampoc no hi hagi cap abonament començat.
        albara.checkPotModificarLinies();
        // Passar de facturable a abonable exigeix, a més, que no se n'hagi facturat res. En sentit
        // contrari aquesta comprovació no es pot fer: el pendent de facturar d'un traspàs abonable és
        // zero per construcció i isHiHaLiniesFacturades el llegiria com a facturació ja començada.
        if (albara.isTraspasFacturable() && liniaAlbaraRepository.isHiHaLiniesFacturades(idAlbara)) {
            throw new TraspasAbonableFacturacioIniciada(idAlbara);
        }
        // Valida que sigui un traspàs amb canvi d'empresa i que no estigui facturat
        var nouAlbara = albara.canviarTraspasAbonable(request.isTraspasAbonable());

        albaraRepository.save(nouAlbara);
        // A Advantage el flag de la capçalera (albcap.trasabon) s'actualitza reinserint l'albcap
        ReplicaAdvantage.instance().addAlbaraDeleteInsert(nouAlbara);

        for (var linia : liniaAlbaraRepository.findByAlbara(idAlbara)) {
            actualitzarLinia(nouAlbara, linia);
        }
    }

    private void actualitzarLinia(Albara albara, LiniaAlbara linia) {
        // Un traspàs abonable no es factura i un de facturable no s'abona: el pendent de facturar passa a
        // zero o torna a ser tota la quantitat de la línia
        var novaLinia = linia.canviarQuantitatPendentFacturar(albara.isTraspasFacturable() ? linia.quantitat() : 0);
        liniaAlbaraRepository.save(novaLinia);
        // A Advantage el pendent de facturar (alblin.penfac) s'actualitza reinserint la línia
        ReplicaAdvantage.instance().addLiniaAlbaraDeleteInsert(novaLinia);

        if (albara.isTraspasAbonable()) {
            ReplicaAdvantage.instance().addPenAboInsert(albara, novaLinia);
        } else {
            ReplicaAdvantage.instance().addPenAboDelete(albara, novaLinia);
        }
    }

    @JsonDeserialize(builder = CanviarTraspasAbonableRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CanviarTraspasAbonableRequest {
        boolean isTraspasAbonable();
    }

}
