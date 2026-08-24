package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.comandes.internal.domain.linia.*;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.request.DefinirStockSeguretatRequest;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.*;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DefinirStockSeguretat {

    ComandaRepository comandaRepo;
    LiniaComandaRepository liniaRepo;
    IObtenirNumeradorComanda obtenirNumeradorComanda;
    IObtenirClientAds obtenirClientAds;
    IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;

    public DefinirStockSeguretat(ComandaRepository comandaRepo, LiniaComandaRepository liniaRepo,
                                 IObtenirNumeradorComanda obtenirNumeradorComanda, IObtenirClientAds obtenirClientAds,
                                 IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda) {
        this.comandaRepo = comandaRepo;
        this.liniaRepo = liniaRepo;
        this.obtenirNumeradorComanda = obtenirNumeradorComanda;
        this.obtenirClientAds = obtenirClientAds;
        this.obtenirArticleClientInformacioComanda = obtenirArticleClientInformacioComanda;
    }

    @Transactional
    public String executar (String articleClientParam, DefinirStockSeguretatRequest req) {
        // Obtenció de l'articleclient a partir dels 13 dígits
        var articleClient = obtenirArticleClientInformacioComanda.executar(articleClientParam).orElseThrow(() -> new ArticleClientNotFound(articleClientParam));
        // Creació de la clau articleclient
        var clauArticleClient = KeyArticleClient.of(articleClient.artint(), articleClient.codiClient());
        // Obtenció del client
        var client = obtenirClientAds.get(clauArticleClient.clicod()).orElseThrow(() -> new ClientNoExisteix(clauArticleClient.clicod()));

        // Obtenció de la línia d'stock de seguretat per l'articleclient
        var optLiniaStockSeg = liniaRepo.findStockSeguretat(clauArticleClient);
        long codiComanda;
        long numLinia;
        if (optLiniaStockSeg.isEmpty()) {
            // En cas de no existir la línia d'stock de seguretat per aquest articleclient
            // cal crear també la comanda des d'on penjarà la línia
            Comanda novaComandaStockSeg = buildNewComandaSeguretat(client, articleClient);
            codiComanda = novaComandaStockSeg.codi();
            numLinia = liniaRepo.nextNumero(codiComanda);
        } else {
            // En cas d'existir s'obté el número de comanda del qual penja la línia
            var liniaStockSeg = optLiniaStockSeg.get();
            codiComanda = liniaStockSeg.comanda();
            numLinia = liniaStockSeg.numero();
        }

        var linia = buildLinia(codiComanda, numLinia, clauArticleClient, articleClient.referencia(), articleClient.divisa(), req);
        liniaRepo.save(linia);

        return linia.codiNumeroFormat();
    }

    private Comanda buildNewComandaSeguretat(ClientAds client, ArticleClientInformacioComandaResponse articleClient) {
        var newCodiComanda = obtenirNumeradorComanda.obtenir();
        ComandaProps props = ComandaPropsImpl.builder()
                .tipus(TipusComanda.PROGRAMA)
                .dades(buildDades(client))
                .informacioClient(buildInfoClient(articleClient))
                .adresa(client.adresaEnviament().orElse(client.adresa()))
                .informacioEnviament(buildInformacioEnviament(client))
                .stockSeguretat(true)
                .servida(false)
                .servible(Servible.NO_APLICA)
                .usuari(RequestThread.nomUsuari())
                .build();
        var comanda = new Comanda(newCodiComanda, props);
        comandaRepo.save(comanda);
        return comanda;
    }

    private DadesComanda buildDades(ClientAds client) {
        return DadesComandaImpl.builder()
                .client(client.clicod())
                .clientNom(client.nom())
                .empresa(client.empresa())
                .dataAlta(RequestThread.dateLocal())
                .build();
    }

    private InformacioClient buildInfoClient(ArticleClientInformacioComandaResponse articleClient) {
        return InformacioClientImpl.builder()
                .identificador(articleClient.aclfab() + articleClient.codiClient())
                .data(RequestThread.dateLocal())
                .programa("")
                .build();
    }

    private InformacioEnviament buildInformacioEnviament(ClientAds client) {
        return InformacioEnviamentImpl.builder()
                .formaEnviament(client.formaEnviament())
                .incoterm(client.incoterm())
                .desti(client.desti())
                .transportista(client.codiTransportista())
                .zonaTransport(client.zonaTransport().map(SimpleItem::codi).orElse(""))
                .build();
    }

    private LiniaComanda buildLinia(long codiComanda, long numLinia, KeyArticleClient clauArticleClient, String referencia, String divisa, DefinirStockSeguretatRequest req) {
        return LiniaComandaImpl.builder()
                .id(KeyLiniaComanda.of(codiComanda, numLinia))
                .articleClient(clauArticleClient)
                .tipusArticleClient(TipusArticleClient.ESPECIAL)
                .tipus(req.stockSeguretatClient() ? TipusLiniaComanda.STOCK_SEG_CLIENT : TipusLiniaComanda.STOCK_SEG_AMES)
                .quantitat(req.quantitat())
                .preu(Preu.of(BigDecimal.ZERO, divisa))
                .isPreuFixat(false)
                .dataSolicitada(req.dataSolicitada())
                .dataPrevistaSortida(req.dataSolicitada())
                .reserva(InformacioReserva.empty())
                .quantitatServida(0)
                .dataCreacio(LocalDateTime.now())
                .referencia(referencia)
                .build();
    }

}
