package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO.LiniaAlbaraTraspasDTO;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.services.CalcularAvisosStockNegatiu.AvisStockNegatiu;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransTraspas.CalcularCreacioAlbaransTraspasResponse;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransTraspas.CreacioNovaLiniaTraspas;
import ames.comercial.albarans.internal.services.IProviderInformacioArticleclient.IProviderInformacioArticleclientResponse;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calcula els avisos d'stock negatiu que provocaria la creació d'una proposta d'albarans de traspàs.
 * <p>
 * És l'anàleg de {@link CalcularAvisosStockNegatiu} per al flux de traspàs: agrega la quantitat total a servir per
 * fitxa d'inventari del <b>magatzem d'origen</b> (empresa d'origen + articleclient), reproduint els moviments de
 * sortida que farà la creació de l'albarà de traspàs (albarans nous, línies noves en albarans de traspàs oberts i
 * increments de línies existents beuen tots de la mateixa fitxa d'origen). Per a cada fitxa on l'stock actual menys
 * la quantitat total a servir quedi per sota de zero, genera un avís. És merament informatiu: no impedeix la creació.
 */
@Service
public class CalcularAvisosStockNegatiuTraspas {

    @Autowired IObtenirStocks obtenirStocks;

    public List<AvisStockNegatiu> executar(CalcularCreacioAlbaransTraspasResponse proposta,
                                           List<AlbaraTraspasAmbLiniesDTO> albaransOberts,
                                           IProviderInformacioArticleclientResponse info,
                                           String magatzemOrigen) {
        // Agregació de la quantitat a servir per (empresa origen, articleclient); el magatzem d'origen és comú
        Map<ClauFitxaAgregada, AcumuladorFitxa> agregat = agregarQuantitatsPerFitxa(proposta, albaransOberts, info);
        if (agregat.isEmpty()) return List.of();

        // Stock actual de totes les fitxes afectades (una sola consulta amb IN per empresa)
        Map<ClauFitxaAgregada, Long> stockActualPerFitxa = obtenirStockActual(agregat.keySet(), magatzemOrigen);

        // Generació dels avisos per a les fitxes que quedarien en negatiu
        List<AvisStockNegatiu> avisos = new ArrayList<>();
        for (var entry : agregat.entrySet()) {
            var clau = entry.getKey();
            var acc = entry.getValue();
            long stockActual = stockActualPerFitxa.getOrDefault(clau, 0L);
            long stockResultant = stockActual - acc.quantitatServir;
            if (stockResultant < 0) {
                avisos.add(new AvisStockNegatiu(
                        acc.matriu, acc.referencia, acc.denominacio,
                        clau.empresa(), magatzemOrigen,
                        stockActual, acc.quantitatServir, stockResultant));
            }
        }
        return avisos;
    }

    private Map<ClauFitxaAgregada, AcumuladorFitxa> agregarQuantitatsPerFitxa(
            CalcularCreacioAlbaransTraspasResponse proposta,
            List<AlbaraTraspasAmbLiniesDTO> albaransOberts,
            IProviderInformacioArticleclientResponse info) {
        Map<ClauFitxaAgregada, AcumuladorFitxa> agregat = new HashMap<>();

        // 1. Albarans nous
        for (var albara : proposta.creacioAlbarans()) {
            for (var linia : albara.linies()) {
                acumular(agregat, albara.empresaOrigen(), linia.articleClient(),
                        linia.matriu(), linia.referencia(), linia.denominacio(), linia.quantitatServir());
            }
        }
        // 2. Línies noves en albarans de traspàs oberts (la clau és l'albarà d'origen)
        for (var entry : proposta.albaransAprofitables().entrySet()) {
            var empresaOrigen = entry.getKey().empresa();
            for (var linia : entry.getValue()) {
                acumular(agregat, empresaOrigen, linia.articleClient(),
                        linia.matriu(), linia.referencia(), linia.denominacio(), linia.quantitatServir());
            }
        }
        // 3. Increments de línies existents d'albarans de traspàs oberts
        Map<KeyLiniaAlbara, LiniaAlbaraTraspasDTO> liniesObertes = new HashMap<>();
        for (var albara : albaransOberts) {
            for (var linia : albara.linies()) {
                liniesObertes.put(linia.idLiniaAlbara(), linia);
            }
        }
        for (var entry : proposta.liniesAprofitables().entrySet()) {
            var empresaOrigen = entry.getKey().idAlbara().empresa();
            var liniaOberta = liniesObertes.get(entry.getKey());
            if (liniaOberta == null) continue;
            var infoArticle = info.get(liniaOberta.articleClient());
            acumular(agregat, empresaOrigen, liniaOberta.articleClient(),
                    infoArticle.matriu(), infoArticle.referencia(), infoArticle.denominacio(), entry.getValue());
        }

        return agregat;
    }

    private Map<ClauFitxaAgregada, Long> obtenirStockActual(Set<ClauFitxaAgregada> claus, String magatzemOrigen) {
        // Agrupació dels articles per empresa per fer una única consulta (amb IN) per empresa
        Map<String, Set<KeyArticleClient>> articlesPerEmpresa = new HashMap<>();
        for (var clau : claus) {
            articlesPerEmpresa.computeIfAbsent(clau.empresa(), k -> new HashSet<>()).add(clau.articleClient());
        }

        Map<ClauFitxaAgregada, Long> stockActualPerFitxa = new HashMap<>();
        for (var entry : articlesPerEmpresa.entrySet()) {
            var empresa = Empresa.getByClau(entry.getKey());
            var stocks = obtenirStocks.queryMagatzem(entry.getValue(), empresa, magatzemOrigen);
            for (var article : entry.getValue()) {
                long stock = stocks.containsKey(article) ? stocks.get(article).stock() : 0L;
                stockActualPerFitxa.put(new ClauFitxaAgregada(entry.getKey(), article), stock);
            }
        }
        return stockActualPerFitxa;
    }

    private void acumular(Map<ClauFitxaAgregada, AcumuladorFitxa> agregat, String empresa,
                          KeyArticleClient articleClient, String matriu, String referencia,
                          String denominacio, long quantitat) {
        var clau = new ClauFitxaAgregada(empresa, articleClient);
        agregat.computeIfAbsent(clau, k -> new AcumuladorFitxa(matriu, referencia, denominacio))
                .quantitatServir += quantitat;
    }

    private record ClauFitxaAgregada(String empresa, KeyArticleClient articleClient) {}

    private static final class AcumuladorFitxa {
        final String matriu;
        final String referencia;
        final String denominacio;
        long quantitatServir;

        AcumuladorFitxa(String matriu, String referencia, String denominacio) {
            this.matriu = matriu;
            this.referencia = referencia;
            this.denominacio = denominacio;
        }
    }

}
