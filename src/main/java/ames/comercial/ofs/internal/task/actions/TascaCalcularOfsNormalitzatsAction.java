package ames.comercial.ofs.internal.task.actions;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.comandes.ext.ObtenirQuantitatPendentServir;
import ames.comercial.comandes.service.Familia;
import ames.comercial.comandes.service.IProviderStocks;
import ames.comercial.ofs.service.*;
import ames.comercial.server.MapperUtils;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Numbers;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TascaCalcularOfsNormalitzatsAction {

    static final Logger log = LogManager.getLogger(TascaCalcularOfsNormalitzatsAction.class.getName());

    private static final String MAGATZEM_NORMALITZATS = "0001";

    @Autowired IProviderStocks providerStocks;
    @Autowired ObtenirQuantitatPendentRebreOf quantitatPendentRebreOf;
    @Autowired ObtenirQuantitatPendentServir quantitatPendentServir;
    @Autowired ObtenirCoeficientCalculOf obtenirCoeficientCalculOf;
    @Autowired ObtenirFamiliesCalculOf obtenirFamiliesCalculOf;
    @Autowired RecalculTerminisOrdreFabricacio recalculTerminisOrdreFabricacio;
    @Autowired CrearNovaOf crearNovaOf;

    @Transactional
    public void executar() {
        try {
            calcul();
        } catch (Exception e) {
            var missatge = "OF_NORMALITZATS Error calculant les OF's de normalitzats";
            log.error(missatge, e);
            throw new AppException(missatge, e);
        }
    }

    private void calcul() {
        // Obtenció de les families que han de calcular OF
        var familiesOf = obtenirFamiliesCalculOf.executar();
        // Obtenció dels articles i filtrat per les families que han de calcular OF's
        var articles = obtenirArticlesClient().stream()
                .filter(a -> familiesOf.contains(a.familia()))
                .toList();
        // Obtenció del coeficient del càlcul per fabricar OF per germans
        var coeficientCalculOf = obtenirCoeficientCalculOf.executar();

        var listResultatCalculOf = new ArrayList<ResultatOfNormalitzat>();
        for (var article : articles) {
            var articleClient = KeyArticleClient.ofNormalitzat(article.artint());
            // Obtenció de l'stock pel magatzem 0001 ja que tots els normalitzats entren per aquest magatzem
            // i s'exclou els que tinguin l'stock negatiu ja que no té sentit tenir l'stock d'un normalitzat
            // en negatiu degut a que no es pot servir si no hi ha la quantitat reservada
            var stock = providerStocks.provide(articleClient, MAGATZEM_NORMALITZATS);
            var qtatPendentRebre = quantitatPendentRebreOf.executar(articleClient);
            var qtatPendentServir = quantitatPendentServir.executar(articleClient, Empresa.GROUP);
            // S'afegeix el càlcul a la llista
            var resultatOf = ResultatOfNormalitzat.of(article, stock, qtatPendentRebre, qtatPendentServir);
            listResultatCalculOf.add(resultatOf.calculaOfPerGerma(coeficientCalculOf));
        }
        // Agrupació dels resultats per matriu (4 primers caràcters)
        var agrupacioMatrius = obtenirLlistaOfCalcular(listResultatCalculOf);
        agrupacioMatrius.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    var listOfs = entry.getValue();
                    var numGermans = listOfs.size();
                    var factorReduccio = new ObtenirFactorReduccioFabricarGermans().executar(numGermans);
                    listOfs.forEach(r -> {
                        var nLot = Numbers.decimal(r.lotOptim())
                                .multiply(factorReduccio)
                                .setScale(0, RoundingMode.HALF_UP)
                                .longValue();
                        var quantitatOfCalculada = r.stockMinim() + nLot + r.qtatPendentServir() - r.stockActual();
                        // La quantitat a fàbrica de la OF és el màxim entre la quantitat calculada
                        // i el mínim de fabricació definit a l'articleclient
                        var quantitatOf = Math.max(quantitatOfCalculada, r.lotMinim());
                        // Obtenció dels terminis de l'última OF no anul·lada
                        var articleClient = KeyArticleClient.ofNormalitzat(r.artint());
                        var terminis = recalculTerminisOrdreFabricacio.executar(articleClient, quantitatOf);
                        // Creació de la OF
                        crearNovaOf.executar(articleClient, r.codiFabrica(), terminis);
                    });
                });
    }

    private Map<String, List<ResultatOfNormalitzat>> obtenirLlistaOfCalcular(List<ResultatOfNormalitzat> list){
        // Agrupació de tots els elements per matriu
        var agrupacioMatriu = list.stream()
                .collect(Collectors.groupingBy(ResultatOfNormalitzat::matriu));
        // Filtre per només deixar les agrupacions que tenen almenys una peça que necessita OF
        // i posterior filtrat per a que a la llista estiguin totes les peces que necessiten OF per ella
        // mateixa o per germà
        return agrupacioMatriu.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(ResultatOfNormalitzat::necessitaOf))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(r -> r.necessitaOf() || r.necessitaOfPerGerma())
                                .collect(Collectors.toList())
                ));
    }

    private List<ArtcliNormalitzat> obtenirArticlesClient() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						SELECT ac.*, n.consmens, n.lotminfab, art.artfam, art.artperfab
						FROM comundb.artcli ac
						LEFT JOIN comundb.art art ON ac.artint = art.artint
						LEFT JOIN normalit n ON ac.artint = n.artint
						WHERE ac.aclflg = 'A'
						    AND (tipus = 'N' or tipus = 'F')
						    AND ac.clicod = '000000'
						""");
        AdvantageDao.ResultSetAction<List<ArtcliNormalitzat>> rsAction = rs -> {
            var result = new ArrayList<ArtcliNormalitzat>();
            while (rs.next()) {
                result.add(ArtcliNormalitzatImpl.builder()
                                .artint(rs.getString("artint"))
                                .aclfab(rs.getString("aclfab"))
                                .familia(Familia.getByFamilia(rs.getString("artfam")))
                                .aclden(rs.getString("aclden"))
                                .aclref(rs.getString("aclref"))
                                .codiFabrica(rs.getString("codfab"))
                                .stockMinim(rs.getLong("aclsts"))
                                .consumMensual(rs.getLong("consmens"))
                                .lotOptim(rs.getLong("acllmin"))
                                .lotMinim(rs.getLong("lotminfab"))
                                .periodeFabricacio(MapperUtils.readOptionalLong(rs, "artperfab").filter(p -> p>0))  // Descarta els períodes 0
                        .build());
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @JsonDeserialize(builder = ArtcliNormalitzatImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface ArtcliNormalitzat {
        String artint();
        String aclfab();
        Familia familia();
        String aclden();
        String aclref();
        String codiFabrica();
        long stockMinim();
        long consumMensual();
        long lotOptim();
        long lotMinim();
        Optional<Long> periodeFabricacio();
    }

    @JsonDeserialize(builder = ResultatOfNormalitzatImpl.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface ResultatOfNormalitzat {
        String artint();
        String aclfab();
        String aclden();
        String aclref();
        String codiFabrica();
        long stockMinim();
        long consumMensual();
        long lotOptim();
        long lotMinim();
        Optional<Long> periodeFabricacio();
        long stockActual();
        long qtatPendentRebreOf();
        long qtatPendentServir();

        @Derived default String matriu() {
            return aclfab().substring(0,4);
        }

        @Derived default long stockFinal() {
            // L'stock final és l'stock actual + la qtat. pendent de rebre d'OFs
            // treien la quantitat pendent de servir
            return stockActual() + qtatPendentRebreOf() - qtatPendentServir();
        }

        @Derived default boolean necessitaOf() {
            return stockFinal() < stockMinim();
        }

        @Default default boolean necessitaOfPerGerma() {
            return false;
        }

        @Default default ResultatOfNormalitzat calculaOfPerGerma(BigDecimal coeficient) {
            // En cas que l'stock mínim estigui establert a 0 no es genera OF ja que vol
            // dir que és una peça que ja no es farà pq té alguna variant
            if (stockMinim() <= 0)
                return this;
            // En cas que si que tingui un stock mínim cal fer el càlcul per si necessitaria fer OF per germà
            // Es multiplica el consum mensual per un factor definit
            var consumMensualMultiplicatFactor = coeficient.multiply(Numbers.decimal(consumMensual())).longValue();
            var stockMinimMesConsum = stockMinim() + consumMensualMultiplicatFactor;
            // Si l'stock final actual està per sota d'aquest valor vol dir que si es fa una OF d'un germà
            // també s'hauria de fer una OF d'aquest mateix article
            var necessitaOfPerGerma = stockFinal() < stockMinimMesConsum;
            return ResultatOfNormalitzatImpl.builder()
                    .from(this)
                    .necessitaOfPerGerma(necessitaOfPerGerma)
                    .build();
        }

        static ResultatOfNormalitzat of(ArtcliNormalitzat artcli, long stockActual, long qtatPendentRebreOf, long qtatPendentServir) {
            return ResultatOfNormalitzatImpl.builder()
                    .artint(artcli.artint())
                    .aclfab(artcli.aclfab())
                    .aclden(artcli.aclden())
                    .aclref(artcli.aclref())
                    .codiFabrica(artcli.codiFabrica())
                    .stockMinim(artcli.stockMinim())
                    .consumMensual(artcli.consumMensual())
                    .lotOptim(artcli.lotOptim())
                    .lotMinim(artcli.lotMinim())
                    .stockActual(stockActual)
                    .qtatPendentRebreOf(qtatPendentRebreOf)
                    .qtatPendentServir(qtatPendentServir)
                    .build();
        }

    }

}
