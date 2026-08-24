package ames.comercial.edi2.internal.application;

import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans;
import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents;
import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.application.service.*;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEdiRepository;
import ames.comercial.edi2.request.MergeComandaRequestImpl;
import ames.comercial.edi2.response.MergeComandaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirComandaPerProcessar {

    @Autowired MergeComandaEstrategia0 mergeComanda;
    @Autowired MergeComandaEstrategia1 mergeComandaEstrategia1;
    @Autowired MergeComandaEstrategia2 mergeComandaEstrategia2;
    @Autowired MergeComandaEstrategia3 mergeComandaEstrategia3;
    @Autowired MergeComandaEstrategia4 mergeComandaEstrategia4;
    @Autowired MergeComandaEstrategia5 mergeComandaEstrategia5;
    @Autowired ObtenirComandaEdi2 obtenirComandaEdi2;
    @Autowired ConfiguracioEntradesEdiRepository configuracioEntradesEdiRepository;
    @Autowired ObtenirCapsaleraEdi obtenirCapsaleraEdi;
    @Autowired IObtenirLiniesComandaPendents comandesPendents;
    @Autowired ObtenirUltimsAlbarans obtenirUltimsAlbarans;

    public MergeComandaResponse executar(KeyComandaEdi keyComandaEdi, String albaraReferencia) throws Exception {
        var comandaEdi = obtenirComandaEdi2.executar(keyComandaEdi).orElseThrow(EDIException.ComandaNoTrobada::new);
        var capsaleraEdi = obtenirCapsaleraEdi.executar(keyComandaEdi.idMissatge());
        var capsaleraComandaEdi = capsaleraEdi.stream()
                .filter(c -> c.idCapsalera() == comandaEdi.idCapsalera())
                .findFirst()
                .orElseThrow(EDIException.CapsaleraNoTrobada::new);

        if (!comandaEdi.potProcessarComanda()) {
            throw new Exception("La comanda no esta pendent de processar");
        }

        var configuracioEdi = configuracioEntradesEdiRepository.obtenirConfiguracioEdi(comandaEdi.articleClient().clicod(),
                capsaleraComandaEdi.tipoMissatge());

        var estrategiaEdi = configuracioEdi.estrategiaEdi();
        var infoSortida = configuracioEdi.informacioSortida();
        var referenciaAlbara = comandaEdi.referenciaAlbara().replaceFirst("^0+", "");
        var referencia = !albaraReferencia.trim().isBlank() ? albaraReferencia : referenciaAlbara;

        var ultimsAlbarans = obtenirUltimsAlbarans.executar(comandaEdi.articleClient(), referencia, comandaEdi.acumulatRebut(),100);

        var request = MergeComandaRequestImpl.builder()
                .liniesComandaActual(comandesPendents.executar(comandaEdi.articleClient(), true))
                .comanda(comandaEdi)
                .infoSortida(infoSortida)
                .dataLimitDiesTall(configuracioEdi.dataLimitDiesTall())
                .ultimsAlbarans(ultimsAlbarans)
                .isConsiderarUltimsAlbarans(configuracioEdi.considerarAlbarans())
                .considerarDuesDates(configuracioEdi.considerarDuesDates())
                .isDuesDates(configuracioEdi.considerarDuesDates())
                .build();
        return switch (estrategiaEdi) {
            case 0 -> mergeComanda.executar(request);
            case 1 -> mergeComandaEstrategia1.executar(request);
            case 2 -> mergeComandaEstrategia2.executar(request);
            case 3 -> mergeComandaEstrategia3.executar(request);
            case 4 -> mergeComandaEstrategia4.executar(request);
            case 5 -> mergeComandaEstrategia5.executar(request);
            default -> throw new IllegalArgumentException("Tipus no vàlid: " + estrategiaEdi);
        };
    }
}