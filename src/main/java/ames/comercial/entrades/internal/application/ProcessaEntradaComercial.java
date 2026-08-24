package ames.comercial.entrades.internal.application;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.advantage.internal.auxiliar.generators.ArtCliGenerator;
import ames.comercial.advantage.internal.auxiliar.generators.ArtGenerator;
import ames.comercial.advantage.internal.response.QueryArticleClientResponse;
import ames.comercial.entrades.events.EntradaArticleEvent;
import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.internal.infraestructure.ConfigArticleClientEntradesRepository;
import ames.comercial.entrades.internal.infraestructure.ConfigFabricaEntradesRepository;
import ames.comercial.entrades.internal.infraestructure.comercial.EntradaComercialRepository;
import ames.comercial.entrades.internal.infraestructure.comercial.ErrorEntradaComercialRepository;
import ames.comercial.inventari.ext.ICrearMovimentEntrada;
import ames.comercial.inventari.internal.domain.service.CrearMovimentEntradaRequestImpl;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentEntrada.CrearMovimentEntradaRequest;
import ames.comercial.ofs.internal.application.command.AplicarEntrada;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.Numbers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessaEntradaComercial {

    @Autowired EntradaComercialRepository entradaComercialRepo;
    @Autowired ErrorEntradaComercialRepository errorEntradaComercialRepo;
    @Autowired OrdreFabricacioRepository ofRepo;
    @Autowired ICrearMovimentEntrada crearMovimentEntrada;
    @Autowired AplicarEntrada aplicarEntradaOf;
    @Autowired ConfigFabricaEntradesRepository configFabricaRepo;
    @Autowired ConfigArticleClientEntradesRepository configArticleClientEntradesRepo;
    @Autowired ApplicationEventPublisher eventPublisher;

    static final String ARTICLE_CLIENT_NO_EXISTEIX = "ARTICLE_CLIENT_NO_EXISTEIX";
    static final String ARTICLE_CLIENT_NO_ACTIU = "ARTICLE_CLIENT_NO_ACTIU";
    static final String CAP_OF_PENDENT = "CAP_OF";
    static final String CONFIGURACIO_FABRICA_EMPRESA_NO_TROBADA = "CONFIGURACIO_FABRICA_EMPRESA_NO_TROBADA";

    @Transactional
    public String processar(EntradaComercial entradaComercial) {
        // La fàbrica ha de tenir configurada una empresa d'entrada
        var optConfigFabrica = configFabricaRepo.get(entradaComercial.fabrica());
        if (optConfigFabrica.isEmpty()) {
            processarError(entradaComercial, CONFIGURACIO_FABRICA_EMPRESA_NO_TROBADA);
            return CONFIGURACIO_FABRICA_EMPRESA_NO_TROBADA;
        }

        // L'articleclient ha d'existir
        var optArticleClient = new ObtenirArticleClientAds().query(entradaComercial.articleFabrica(), entradaComercial.client());
        if (optArticleClient.isEmpty()) {
            processarError(entradaComercial, ARTICLE_CLIENT_NO_EXISTEIX);
            return ARTICLE_CLIENT_NO_EXISTEIX;
        }
        var articleClient = optArticleClient.get();

        // L'article client ha d'estar actiu
        if (!articleClient.isActiu()) {
            processarError(entradaComercial, ARTICLE_CLIENT_NO_ACTIU);
            return ARTICLE_CLIENT_NO_ACTIU;
        }

        // Resolució de l'empresa: override per articleclient+fàbrica si existeix, sinó la de config_fabrica
        // Això es deixa preparat per casos on l'empresa d'entrada sigui diferent per articleclient+fàbrica, com
        // quan existia AMES PORE o similar, que una peça es feia en una fàbrica i entrava per una empresa diferent
        // a la que hi ha ara al config_fabrica, però això no permetia que un articleclient fabricat a dos fàbriques difernents
        // es pogués entrar per dos empreses diferents, ja que l'articleclient només pot tenir una empresa.
        // Per això es deixa la possibilitat de fer override per articleclient+fàbrica.
        var empresa = configArticleClientEntradesRepo
                .getEmpresa(articleClient.clau(), entradaComercial.fabrica())
                .orElse(optConfigFabrica.get().empresa());

        // L'OF ha d'existir per artint i clicod
        var optOf = ofRepo.get(articleClient.clau());
        if (optOf.isEmpty()) {
            processarError(entradaComercial, CAP_OF_PENDENT);
            return CAP_OF_PENDENT;
        }
        var of = optOf.get();

        // S'actualitza l'OF amb l'entrada
        aplicarEntradaOf.executar(articleClient.clau(), entradaComercial.quantitat());

        // Marca l'entrada com a processada
        entradaComercialRepo.save(entradaComercial.processa());

        // Registre de l'entrada a l'inventari
        crearMovimentEntrada.executar(buildRequestEntrada(articleClient, empresa, entradaComercial, of.numero()));

        // Es publica un event per notificar que s'ha afegit una entrada
        eventPublisher.publishEvent(new EntradaArticleEvent(this, articleClient.clau(), Empresa.getByClau(empresa)));

        // Actualització de l'Advantage
        List<PreparedStatementProvider> statements = new ArrayList<>();
        // ARTCLI - Actualització de  l'acumulat total per entrades (aclacue)
        statements.add(ArtCliGenerator.updateAcumulatEntrades(articleClient.clau(), entradaComercial.quantitat()));
        // ART - Pes premsat
        if (Numbers.isHigh(entradaComercial.pesPremsat()).than(0))
            statements.add(ArtGenerator.updatePesPremsat(articleClient.artInt(), entradaComercial.pesPremsat()));
        // ART - Pes final
        if (Numbers.isHigh(entradaComercial.pesFinal()).than(0))
            statements.add(ArtGenerator.updatePesFinal(articleClient.artInt(), entradaComercial.pesFinal()));

        // Update a Advantage
        new AdvantageDao().executeUpdate(statements);

        return "";
    }

    private CrearMovimentEntradaRequest buildRequestEntrada(QueryArticleClientResponse articleClient, String empresa, EntradaComercial entradaComercial, long numeroOf) {
        return CrearMovimentEntradaRequestImpl.builder()
                .articleClient(articleClient.clau())
                .empresa(empresa)
                .magatzem(entradaComercial.magatzem())
                .data(entradaComercial.dataEntrada())
                .quantitat(entradaComercial.quantitat())
                .of(numeroOf)
                .idEntrada(entradaComercial.id())
                .idEntradaFabrica(entradaComercial.idEntradaFabrica())
                .build();
    }

    private void processarError(EntradaComercial entradaComercial, String codiError) {
        entradaComercialRepo.save(entradaComercial.processaError(codiError));
        errorEntradaComercialRepo.guardarError(entradaComercial.idEntradaFabrica());
    }

}
