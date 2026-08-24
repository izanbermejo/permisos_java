package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.AlbaransException.AlbaraTancat;
import ames.comercial.albarans.AlbaransException.LiniaAlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.eliminaralbara.EstrategiesEliminarAlbara;
import ames.comercial.inventari.internal.application.command.DesferMoviments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Elimina una línia concreta d'un albarà: esborra la línia, desfà el moviment de sortida associat
 * (que restaura l'estoc i la quantitat pendent de la línia de comanda) i replica l'eliminació a
 * l'Advantage. Si l'albarà queda sense línies, també s'elimina la capçalera.
 */
@Service
public class EliminarLiniaAlbara {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired DesferMoviments desferMoviments;
    @Autowired EstrategiesEliminarAlbara estrategiesEliminarAlbara;

    @Transactional
    public void executar(KeyLiniaAlbara idLinia) {
        var idAlbara = idLinia.idAlbara();
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        // No es permet modificar les línies d'un albarà facturat ni amb moviments de magatzem
        // actius (missatges d'error diferenciats). Es comprova abans del tancat perquè aquests
        // motius són més específics.
        albara.checkPotModificarLinies();
        // No es permet eliminar línies d'un albarà tancat (control únicament al backend)
        if (albara.isTancat()) throw new AlbaraTancat(idAlbara);
        var linia = liniaAlbaraRepository.find(idLinia).orElseThrow(() -> new LiniaAlbaraNoExisteix(idLinia));

        // Eliminació de la línia
        liniaAlbaraRepository.delete(idLinia);
        // Es desfà el moviment de sortida (i la línia de comanda associada)
        desferMoviments.executar(List.of(idLinia));
        // Pas específic segons el tipus d'albarà (p. ex. consum: restablir el pendent de consumir del
        // traspàs; traspàs d'empresa abonable: treure'n el pendent d'abonar)
        estrategiesEliminarAlbara.get(albara.tipus()).desferLinia(albara, linia);
        // Rèplica Advantage de l'eliminació de la línia
        ReplicaAdvantage.instance().addLiniaAlbaraDelete(linia);

        // Si l'albarà queda sense línies, s'elimina també la capçalera
        if (liniaAlbaraRepository.findByAlbara(idAlbara).isEmpty()) {
            albaraRepository.delete(idAlbara);
            ReplicaAdvantage.instance().addAlbaraDelete(albara);
        }
    }

}
