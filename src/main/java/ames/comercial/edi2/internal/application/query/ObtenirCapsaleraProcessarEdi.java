package ames.comercial.edi2.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.edi2.response.CapsaleraProcessarEDIResponse;
import ames.comercial.edi2.response.CapsaleraProcessarEDIResponseImpl;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.SharedExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirCapsaleraProcessarEdi {

    @Autowired IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;

    public CapsaleraProcessarEDIResponse executar(String articleClient){
        var info = obtenirArticleClientInformacioComanda.executar(articleClient)
                .orElseThrow(() -> new SharedExceptions.ArticleClientNotFound(articleClient));

        return CapsaleraProcessarEDIResponseImpl.builder()
                .artInt(info.artint())
                .article(info.aclfab())
                .codiClient(info.codiClient())
                .magatzemEntrada(info.magatzemEntrada())
                .magatzemEntradaDesc(info.magatzemEntradaDesc())
                .empresa(Empresa.getByClau(info.codiEmpresa()))
                .denominacio(info.denominacio())
                .nivellTecnic(info.nivellTecnic())
                .fabrica(info.codiFabrica() + " - " + info.descFabrica())
                .empresaFacturacio(info.codiEmpresa() + " - " + info.descFabrica())
                .unitatsEmbalatge(info.unitatsEmbalatge())
                .caixesPalet(info.numCaixesPalet())
                .formaEnviament(info.formaEnviament())
                .incoterm(info.incoterm())
                .desti(info.desti())
                .stockTotal(info.stockTotal())
                .diesTransit(info.diesTransitClient())
                .diesSortida(info.diesSortida())
                .transportista(info.codiTransportista())
                .magatzemSortida(info.magatzemSortida())
                .magatzemSortidaDesc(info.magatzemSortidaDesc())
                .build();
    }
}
