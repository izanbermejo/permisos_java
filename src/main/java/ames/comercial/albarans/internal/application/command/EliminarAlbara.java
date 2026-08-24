package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.eliminaralbara.EstrategiesEliminarAlbara;
import ames.comercial.inventari.internal.application.command.DesferMoviments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarAlbara {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired DesferMoviments desferMoviments;
    @Autowired EstrategiesEliminarAlbara estrategiesEliminarAlbara;

    @Transactional
    public void executar(KeyAlbara idAlbara) {
        // TODO Comprovar si es pot eliminar
        // Obtenció de l'albarà i les seves línies
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        // Un albarà que el procés del SII ja ha declarat a la hisenda pública no es pot eliminar
        albara.checkNoPresentatHisenda();
        var liniesAlbara = liniaAlbaraRepository.findByAlbara(idAlbara);
        var idLiniesAlbara = liniesAlbara.stream().map(LiniaAlbara::id).toList();
        // Eliminació de l'albarà i les seves línies
        albaraRepository.delete(idAlbara);
        liniaAlbaraRepository.deleteByAlbara(idAlbara);
        // Es desfa el moviment de sortida (i es desfà la línia de comanda associada)
        desferMoviments.executar(idLiniesAlbara);
        // Pas específic segons el tipus d'albarà (p. ex. consum: restablir el pendent de consumir dels
        // traspassos; traspàs d'empresa abonable: treure'n els pendents d'abonar)
        estrategiesEliminarAlbara.get(albara.tipus()).desferAlbara(albara, liniesAlbara);
        // Rèplica Advantage (eliminar l'albarà i les línies)
        ReplicaAdvantage.instance().addAlbaraDelete(albara);
        liniesAlbara.forEach(ReplicaAdvantage.instance()::addLiniaAlbaraDelete);
    }

}
