package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.consum.SortidaPlataforma;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.consum.SortidaPlataformaRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desfà un consum de plataforma: a partir dels registres de {@code albarans.sortides_plataforma},
 * restableix el pendent de consumir a les línies dels albarans de traspàs corresponents i esborra
 * els registres. S'invoca en eliminar un albarà (o una línia) de consum.
 */
@Service
public class DesferConsumPlataforma {

    @Autowired SortidaPlataformaRepository sortidaPlataformaRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;

    public void executarPerAlbara(KeyAlbara albaraConsum) {
        restablirPendent(sortidaPlataformaRepository.findByAlbaraConsum(albaraConsum));
        sortidaPlataformaRepository.deleteByAlbaraConsum(albaraConsum);
    }

    public void executarPerLinia(KeyLiniaAlbara liniaConsum) {
        restablirPendent(sortidaPlataformaRepository.findByLiniaConsum(liniaConsum));
        sortidaPlataformaRepository.deleteByLiniaConsum(liniaConsum);
    }

    private void restablirPendent(List<SortidaPlataforma> sortides) {
        for (var sortida : sortides) {
            liniaAlbaraRepository.find(sortida.albaraTraspas()).ifPresent(liniaTraspas -> {
                var restaurada = liniaTraspas.desferConsumir(sortida.quantitat());
                liniaAlbaraRepository.save(restaurada);
                ReplicaAdvantage.instance().addLiniaAlbaraDeleteInsert(restaurada);
            });
        }
    }

}
