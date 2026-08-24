package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
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
 * Calcula els avisos d'stock negatiu que provocaria la creació d'una proposta d'albarans de sortida.
 * <p>
 * Agrega la quantitat total a servir per fitxa d'inventari (empresa + magatzem + articleclient), reproduint
 * exactament els moviments de sortida que farà {@link ames.comercial.albarans.internal.application.command.CrearAlbara}
 * (albarans nous, línies noves en albarans oberts i increments de línies existents beuen tots de la mateixa fitxa).
 * Per a cada fitxa on l'stock actual menys la quantitat total a servir quedi per sota de zero, genera un avís.
 * És merament informatiu: no impedeix la creació.
 */
@Service
public class CalcularAvisosStockNegatiu {

    @Autowired IObtenirStocks obtenirStocks;

    public List<AvisStockNegatiu> executar(CalcularCreacioAlbaransSortidaResponse proposta,
                                           Map<KeyLiniaComanda, Long> quantitats,
                                           String magatzem) {
        // Agregació de la quantitat a servir per (empresa, articleclient); el magatzem és comú a tota la proposta
        Map<ClauFitxaAgregada, AcumuladorFitxa> agregat = agregarQuantitatsPerFitxa(proposta, quantitats);
        if (agregat.isEmpty()) return List.of();

        // Stock actual de totes les fitxes afectades (una sola consulta amb IN per empresa)
        Map<ClauFitxaAgregada, Long> stockActualPerFitxa = obtenirStockActual(agregat.keySet(), magatzem);

        // Generació dels avisos per a les fitxes que quedarien en negatiu
        List<AvisStockNegatiu> avisos = new ArrayList<>();
        for (var entry : agregat.entrySet()) {
            var clau = entry.getKey();
            var acc = entry.getValue();
            long stockActual = stockActualPerFitxa.getOrDefault(clau, 0L);
            long stockResultant = stockActual - acc.quantitatServir;
            if (stockResultant < 0) {
                avisos.add(new AvisStockNegatiu(
                        acc.projecte, acc.referencia, acc.denominacio,
                        clau.empresa(), magatzem,
                        stockActual, acc.quantitatServir, stockResultant));
            }
        }
        return avisos;
    }

    private Map<ClauFitxaAgregada, AcumuladorFitxa> agregarQuantitatsPerFitxa(
            CalcularCreacioAlbaransSortidaResponse proposta, Map<KeyLiniaComanda, Long> quantitats) {
        Map<ClauFitxaAgregada, AcumuladorFitxa> agregat = new HashMap<>();

        // 1. Albarans nous
        for (var albara : proposta.creacioAlbarans()) {
            for (var linia : albara.liniesAlbara()) {
                acumular(agregat, albara.empresa().clau(), linia.articleClient(), linia.liniesComanda().get(0), linia.quantitatServir());
            }
        }
        // 2. Línies noves en albarans oberts
        for (var entry : proposta.albaransAprofitables().entrySet()) {
            var empresa = entry.getKey().empresa();
            for (var linia : entry.getValue()) {
                acumular(agregat, empresa, linia.articleClient(), linia.liniesComanda().get(0), linia.quantitatServir());
            }
        }
        // 3. Increments de línies existents d'albarans oberts
        for (var entry : proposta.liniesAprofitables().entrySet()) {
            var empresa = entry.getKey().idAlbara().empresa();
            var liniesComanda = entry.getValue();
            var representant = liniesComanda.get(0);
            long quantitatServir = liniesComanda.stream()
                    .mapToLong(l -> quantitats.getOrDefault(l.id(), 0L))
                    .sum();
            acumular(agregat, empresa, representant.articleClient(), representant, quantitatServir);
        }

        return agregat;
    }

    private Map<ClauFitxaAgregada, Long> obtenirStockActual(Set<ClauFitxaAgregada> claus, String magatzem) {
        // Agrupació dels articles per empresa per fer una única consulta (amb IN) per empresa
        Map<String, Set<KeyArticleClient>> articlesPerEmpresa = new HashMap<>();
        for (var clau : claus) {
            articlesPerEmpresa.computeIfAbsent(clau.empresa(), k -> new HashSet<>()).add(clau.articleClient());
        }

        Map<ClauFitxaAgregada, Long> stockActualPerFitxa = new HashMap<>();
        for (var entry : articlesPerEmpresa.entrySet()) {
            var empresa = Empresa.getByClau(entry.getKey());
            var stocks = obtenirStocks.queryMagatzem(entry.getValue(), empresa, magatzem);
            for (var article : entry.getValue()) {
                long stock = stocks.containsKey(article) ? stocks.get(article).stock() : 0L;
                stockActualPerFitxa.put(new ClauFitxaAgregada(entry.getKey(), article), stock);
            }
        }
        return stockActualPerFitxa;
    }

    private void acumular(Map<ClauFitxaAgregada, AcumuladorFitxa> agregat, String empresa,
                          KeyArticleClient articleClient, InformacioLiniaComandaDTO liniaComanda, long quantitat) {
        var clau = new ClauFitxaAgregada(empresa, articleClient);
        agregat.computeIfAbsent(clau, k -> new AcumuladorFitxa(
                liniaComanda.projecte(), liniaComanda.referencia(), liniaComanda.denominacio()))
                .quantitatServir += quantitat;
    }

    private record ClauFitxaAgregada(String empresa, KeyArticleClient articleClient) {}

    private static final class AcumuladorFitxa {
        final String projecte;
        final String referencia;
        final String denominacio;
        long quantitatServir;

        AcumuladorFitxa(String projecte, String referencia, String denominacio) {
            this.projecte = projecte;
            this.referencia = referencia;
            this.denominacio = denominacio;
        }
    }

    public record AvisStockNegatiu(
            String article,
            String referencia,
            String denominacio,
            String empresa,
            String magatzem,
            long stockActual,
            long quantitatServir,
            long stockResultant
    ) {}

}
