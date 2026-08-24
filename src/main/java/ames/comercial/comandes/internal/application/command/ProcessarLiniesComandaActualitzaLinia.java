package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComandaImpl;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessarLiniesComandaActualitzaLinia {

    @Autowired LiniaComandaRepository liniaComandaRepo;
    @Autowired ActualitzarComentarisClientLiniaComanda actialitzarComentsClient;
    @Autowired ActualitzarComentarisInternsLiniaComanda actialitzarComentsIntern;

    public void executar(ProcessarLiniesComandaReq procLinia) {
        // 1.- Obtenció de la línia actual
        var liniaActual = liniaComandaRepo.find(procLinia.clauLinia().orElseThrow()).orElseThrow(LiniaComandaNoExisteix::new);
        // 2.- Comprovar si ha canviat algun dels camps que es poden canviar a l'EDI
        var hasChanged = hasChanged(liniaActual, procLinia);
        // 3.- En cas que s'hagi canviat algun dels camps s'apliquen a la linia i es guarden a BBDD
        if (hasChanged) {
            var newLinia = applyChangesLinia(liniaActual, procLinia);
            liniaComandaRepo.save(newLinia);
            if (procLinia.comentarisClient().isPresent()) actialitzarComentsClient.executar(liniaActual.id(), procLinia.comentarisClient().get());
            if (procLinia.comentarisInterns().isPresent()) actialitzarComentsIntern.executar(liniaActual.id(), procLinia.comentarisInterns().get());
        }
    }

    private boolean hasChanged (LiniaComanda linia, ProcessarLiniesComandaReq procLinia) {
        return !(linia.quantitat() == procLinia.quantitat()
                && linia.tipus().equals(procLinia.tipus())
                && linia.dataSolicitada().equals(procLinia.dataSolicitada())
                && linia.dataPrevistaSortida().equals(procLinia.dataPrevistaSortida())
                && linia.dataPrevistaSortidaInterna().equals(procLinia.dataPrevistaSortidaInterna()));
    }

    private LiniaComanda applyChangesLinia (LiniaComanda linia, ProcessarLiniesComandaReq procLinia) {
        // La quantitat és el màxim entre la quantitat proposada i la servida.
        // Ja que en una modificació la quantitat no pot ser inferior a la quantitat servida
        return LiniaComandaImpl.builder()
                .from(linia)
                .quantitat(linia.quantitatServida() + procLinia.quantitat())
                .tipus(procLinia.tipus())
                .dataSolicitada(procLinia.dataSolicitada())
                .dataPrevistaSortida(procLinia.dataPrevistaSortida())
                .dataPrevistaSortidaInterna(procLinia.dataPrevistaSortidaInterna())
                .build();
    }

}
