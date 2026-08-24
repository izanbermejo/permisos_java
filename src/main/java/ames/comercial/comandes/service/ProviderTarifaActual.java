package ames.comercial.comandes.service;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.IObtenirTarifesDefinidesAds;
import ames.comercial.comandes.internal.application.query.ObtenirTarifesGlobalsActuals;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import ames.comercial.shared.SharedExceptions.TarifaFiltresNoDefinidaEmpresaDivisa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderTarifaActual implements IProviderTarifaActual{

    @Autowired IObtenirClientAds obtenirClientAds;
    @Autowired IObtenirTarifesDefinidesAds tarifesDefinidesAds;
    @Autowired ObtenirTarifesGlobalsActuals obtenirTarifesGlobalsActuals;

    @Override
    public ProviderTarifaActualResponse executar(String clicod) {
        var client = obtenirClientAds.get(clicod).orElseThrow(() -> new ClientNoExisteix(clicod));
        var empresa = Empresa.getByClau(client.empresa());
        var tarifesAds = tarifesDefinidesAds.get(clicod).orElseThrow(() -> new ClientNoExisteix(clicod));
        // Si és un client de MEDICAL s'agafa la tarifa del cli6 i en cas que no la tingui la del país
        if (Empresa.MEDICAL.equals(empresa)){
            return ProviderTarifaActualResponseImpl.builder()
                    .medical(tarifesAds.tarifaCli6().or(tarifesAds::tarifaCmatPais))
                    .build();
        }
        // Obtenció de les tarifes globals
        var tarifesGlobals = obtenirTarifesGlobalsActuals.executar(empresa, client.divisa())
                .orElseThrow(() -> new TarifaFiltresNoDefinidaEmpresaDivisa(empresa, client.divisa()));
        // Si es tracta d'IBINSA s'agafa la seva tarifa especial del SETUP i per coxinets i barres
        // la que tingui definida al cli6 i en cas que no al tingui la del país
        if (client.clicod().equals("038401")) {
            return ProviderTarifaActualResponseImpl.builder()
                    .ibinsa(tarifesGlobals.ibinsa())
                    .coixinets(tarifesAds.tarifaCli6().or(tarifesAds::tarifaPais))
                    .barres(tarifesAds.tarifaBCli6().or(tarifesAds::tarifaBPais))
                    .filtresBxx(tarifesGlobals.filtresBxxDistribuidor())
                    .filtresSsu(tarifesGlobals.filtresSsuDistribuidor())
                    .filtresSxx(tarifesGlobals.filtresSxxDistribuidor())
                    .filtresSsuPlaques(tarifesGlobals.filtresSsuPlaques())
                    .build();
        }
        // Si és un client Web s'agafa la tarifa per defecte establerta
        if (client.isClientWeb()) {
            return ProviderTarifaActualResponseImpl.builder()
                    .coixinets(tarifesGlobals.coixinetsWeb())
                    .barres(tarifesGlobals.barresWeb())
                    .filtresBxx(tarifesGlobals.filtresBxxClient())
                    .filtresSsu(tarifesGlobals.filtresSsuClient())
                    .filtresSxx(tarifesGlobals.filtresSxxClient())
                    .filtresSsuPlaques(tarifesGlobals.filtresSsuPlaques())
                    .build();
        }
        // Si no és cap dels casos anteriors i és un distribuidor s'aplica el que tingui definit al cli6
        // i al país i les tarifes de filtres per distribuidors
        if (client.isDistribuidor() || client.isSuministramentIndustrial()) {
            return ProviderTarifaActualResponseImpl.builder()
                    .coixinets(tarifesAds.tarifaCli6().or(tarifesAds::tarifaPais))
                    .barres(tarifesAds.tarifaBCli6().or(tarifesAds::tarifaBPais))
                    .filtresBxx(tarifesGlobals.filtresBxxDistribuidor())
                    .filtresSsu(tarifesGlobals.filtresSsuDistribuidor())
                    .filtresSxx(tarifesGlobals.filtresSxxDistribuidor())
                    .filtresSsuPlaques(tarifesGlobals.filtresSsuPlaques())
                    .build();
        }
        return ProviderTarifaActualResponseImpl.builder()
                .coixinets(tarifesAds.tarifaCli6().or(tarifesAds::tarifaPais))
                .barres(tarifesAds.tarifaBCli6().or(tarifesAds::tarifaBPais))
                .filtresBxx(tarifesGlobals.filtresBxxClient())
                .filtresSsu(tarifesGlobals.filtresSsuClient())
                .filtresSxx(tarifesGlobals.filtresSxxClient())
                .filtresSsuPlaques(tarifesGlobals.filtresSsuPlaques())
                .build();
    }


}
