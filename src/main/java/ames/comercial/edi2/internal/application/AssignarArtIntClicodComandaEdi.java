package ames.comercial.edi2.internal.application;

import ames.comercial.advantage.internal.ObtenirArticleClientEDIAds;
import ames.comercial.advantage.internal.response.QueryArticleClientEDIResponse;
import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;
import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.capsalera.CapsaleraEdiRepository;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEdiRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AssignarArtIntClicodComandaEdi {

    @Autowired CapsaleraEdiRepository capsaleraEdiRepository;
    @Autowired ComandaEdiRepository comandaEdiRepository;
    @Autowired
    ConfiguracioEntradesEdiRepository configuracioEntradesEdiRepository;
    @Autowired ObtenirArticleClientEDIAds obtenirArticleClientEDIAds;

    public Optional<String> processarComanda(KeyComandaEdi comanda) {
        ComandaEdi comandaActual = comandaEdiRepository.obtenirComanda(comanda)
                .orElseThrow(EDIException.ComandaNoTrobada::new);
        CapsaleraEdi capsaleraEdi = capsaleraEdiRepository
                .obtenir(comandaActual.idMissatge(), comandaActual.idCapsalera())
                .orElseThrow(EDIException.CapsaleraNoTrobada::new);

        // Obtenir les configuracions EDI segons les dades del missatge
        var configuracions = configuracioEntradesEdiRepository.obtenirConfiguracioEdi(
                capsaleraEdi.edibox(),
                comandaActual.nad02(),
                capsaleraEdi.codiProveidor(),
                capsaleraEdi.tipoMissatge()
        );

        // Buscar totes les peces que tenen la referència de totes les configuracions trobades
        List<String> codiClients = configuracions.stream().map(ConfiguracioEntradaComanda::codiClient).toList();
        String cliCod = codiClients.size() == 1 ? codiClients.get(0) : null;

        if (configuracions.isEmpty()){
            comandaEdiRepository.save(comandaActual.marcarError("CONFIGURACIOEDI_NO_TROBADA", Optional.empty()));
            return Optional.of("CONFIGURACIOEDI_NO_TROBADA");
        }

        List<QueryArticleClientEDIResponse> articles = obtenirArticleClientEDIAds
                .findByCliCodsAclRef(codiClients, comandaActual.idArticleComprador());

        // Si no hi ha cap peça...
        if (articles.isEmpty()) {
            comandaEdiRepository.save(comandaActual.marcarError("ARTICLE_NO_TROBAT", Optional.ofNullable(cliCod)));
            return Optional.of("ARTICLE_NO_TROBAT");
        }

        // Només s'ha trobat un articleclient per totes les configuracions possibles, per
        // tant queda lligat amb aquest
        if (articles.size() == 1) {
            var articleClient = articles.get(0);
            comandaEdiRepository.save(comandaActual.lligar(articleClient.clau()));
            return Optional.empty();
        }

        // Si s'arriba aquí vol dir que hem trobat més d'un articleclient amb la mateixa referència
        // Queda comprovar si podem desempatar por el lugar d'entrega
        var optArticleClientDesempat = desempatarLlocEntrega(comandaActual.llocEntrega(), articles, configuracions);
        if (optArticleClientDesempat.isPresent()) {
            var articleClient = KeyArticleClient.of(optArticleClientDesempat.get().artInt(), optArticleClientDesempat.get().clicod());
            comandaEdiRepository.save(comandaActual.lligar(articleClient));
        } else {
            comandaEdiRepository.save(comandaActual.marcarError("VARIS_ARTICLES_TROBATS", Optional.ofNullable(cliCod)));
            return Optional.of("VARIS_ARTICLES_TROBATS");
        }

        return Optional.empty();
    }

    private Optional<QueryArticleClientEDIResponse> desempatarLlocEntrega(String lugarEntrega,
            List<QueryArticleClientEDIResponse> articlesClient,
            List<ConfiguracioEntradaComanda> configuracioEntradaComandas) {
        // Filtrar les configuracions per lugar d'entrega
        var configuracionsAmbLlocEntrega = configuracioEntradaComandas.stream()
                .filter(c -> c.isAcceptaLlocEntrega(lugarEntrega))
                .toList();

        // Si hi han varis no es pot desempatar
        if (configuracionsAmbLlocEntrega.isEmpty())
            return Optional.empty();

        // Si només hi ha un filtrem si amb aquest codi hi ha algun articleclient
        var codiClientCandidat = configuracionsAmbLlocEntrega.get(0).codiClient();
        var articles = articlesClient.stream()
                .filter(a -> a.clicod().equals(codiClientCandidat))
                .toList();

        // Si només es troba un, s'ha aconseguit desempatar
        if (articles.size() == 1)
            return Optional.of(articles.get(0));

        // Si n'hi ha mes d'un no s'ha pogut desempatar
        return Optional.empty();
    }

}