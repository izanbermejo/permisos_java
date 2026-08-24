package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.infraestructure.ConfigFabricaEntradesRepository;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;
import ames.comercial.entrades.internal.domain.EntradaMagatzemDetallImpl;
import ames.comercial.entrades.internal.domain.EntradaMagatzemEmbalatgeImpl;
import ames.comercial.entrades.internal.domain.EntradaMagatzemImpl;
import ames.comercial.server.Json;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Numbers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntradaMagatzemParser {

    ConfigFabricaEntradesRepository configFabricaRepo;
    String idEntradaFabrica;
    Json json;

    public EntradaMagatzemParser (ConfigFabricaEntradesRepository configFabricaRepo, Json json) {
        this.configFabricaRepo = configFabricaRepo;
        this.json = json;
    }

    public List<EntradaMagatzem> parse (String idEntradaFabrica, String contingut) {
        this.idEntradaFabrica = idEntradaFabrica;
        // Obtenció de l'estructura JSON enviada
        var capsaleres = json.deserialize(contingut, new TypeReference<List<MissatgeEntradaCapsalera>>() {});
        // Parseig per convertir l'estructura JSON enviada a l'estructura d'EntradaMagatzem definida
        return parseEntrades(capsaleres);
    }

    private List<EntradaMagatzem> parseEntrades(List<MissatgeEntradaCapsalera> capsaleres) {
        String magatzemEntrada = "";
        String codiFabrica = "";
        List<EntradaMagatzem> result = new ArrayList<>();
        // Recorregut de cada línia
        for (var cap : capsaleres) {
            // En la primera línea s'obté el codi de fàbrica i a partir d'aquesta
            // s'obté el magatzem d'entrada
            if (magatzemEntrada.isBlank()) {
                codiFabrica = cap.empresa();
                magatzemEntrada = obtenirMagatzemEntrada(codiFabrica);
            }
            result.add(parseEntradaMagatzem(cap, codiFabrica, magatzemEntrada));
        }
        return result;
    }

    private String obtenirMagatzemEntrada(String codiFabrica) {
        return configFabricaRepo.get(codiFabrica)
                .map(c -> c.magatzem())
                .orElseThrow(() -> new AppException(
                        String.format("Error parsejant fitxer entrades. Fàbrica amb codi %s no trobada a la configuració d'entrades (entrades.config_fabrica)", codiFabrica)));
    }

    private EntradaMagatzem parseEntradaMagatzem(MissatgeEntradaCapsalera capsalera, String fabrica, String magatzem) {
        return EntradaMagatzemImpl.builder()
                .idEntradaFabrica(idEntradaFabrica)
                .articleFabrica(capsalera.codiArticle())
                .client(capsalera.codiClient())
                .magatzem(magatzem)
                .fabrica(fabrica)
                .quantitat(capsalera.quantitat())
                .quantitatCaixa(capsalera.quantitatCaixa())
                .dataEtiqueta(capsalera.isPaletHomogeni() ? capsalera.dataPal() : capsalera.dataEti())
                .lot(capsalera.numLot().orElse(""))
                .of(capsalera.comanda())
                .etiquetaCaixa(capsalera.etiquetaCaixa())
                .etiquetaPalet(capsalera.numPalet())
                .nivellTecnic(capsalera.nivellTecnic())
                .codiSeguretat(capsalera.codiSeguretat())
                .codiCal(capsalera.aqptxt())
                .dataEntrada(capsalera.dataLliurament())
                .pesPremsat(capsalera.pesPremsatKg())
                .pesFinal(capsalera.pesFinalKg())
                .detalls(capsalera.children().stream()
                        .map(this::parseDetall)
                        .toList())
                .embalatges(buildEmbalatges(capsalera))
                .build();
    }

    private EntradaMagatzemDetall parseDetall (MissatgeEntradaDetall detall) {
        var cantot = detall.quantitatCaixa();
        var dateti = detall.dataEti();
        var lote   = detall.numLot();
        var of     = detall.comanda();
        var eticaj = detall.numeroEtiqueta();
        return EntradaMagatzemDetallImpl.builder()
                .etiquetaCaixa(eticaj)
                .quantitat(cantot)
                .dataEtiqueta(dateti)
                .lot(lote)
                .of(of)
                .build();
    }

    private List<EntradaMagatzemEmbalatge> buildEmbalatges (MissatgeEntradaCapsalera capsalera) {
        // Parseig dels embalatges de la capçalera (no estan dins de cap detall sino que pertanyen a la capçalera,
        // son retornables del palet com per exemple una tapa del palet)
        var listEmbalatgesCapsalera = capsalera.retornables().stream()
                .map(this::parseEmbalatge);
        // Parseig dels emablatges de tots el detalls (están dins de cada detall, com per exemple embalatges
        // de cadascuna de les caixes del palet)
        var listEmbalatgesDetalls = capsalera.children().stream()
                .flatMap(detall -> detall.retornables().stream())
                .map(this::parseEmbalatge);
        // Es retorna la llista resultant dels retornables de la capçalera i de cadascun dels seus detalls
        return Stream.concat(listEmbalatgesCapsalera, listEmbalatgesDetalls)
                    .toList();
    }

    private EntradaMagatzemEmbalatge parseEmbalatge (MissatgeEntradaRetornable retornable) {
        var article = retornable.codiArticle();
        var client = retornable.codiClient();
        var codiElement = retornable.codiComponent();
        var etiqCaixa = retornable.numeroEtiqueta();
        var etiqPalet = retornable.numPalet();
        var descripcio = retornable.nomComponent();
        var quantitat = retornable.quantitatComponent();
        return EntradaMagatzemEmbalatgeImpl.builder()
                .article(article)
                .client(client)
                .codiElement(codiElement)
                .etiquetaCaixa(etiqCaixa)
                .etiquetaPalet(etiqPalet)
                .descripcio(descripcio)
                .quantitat(quantitat)
                .build();
    }

    @JsonDeserialize(builder = MissatgeEntradaCapsaleraImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface MissatgeEntradaCapsalera {
        String tipus();
        Optional<Long> quantitatPalet();
        long quantitatCaixa();
        long comanda();
        LocalDate dataLliurament();
        Optional<Long> numeroEtiqueta();
        long numPalet();
        LocalDate dataEti();
        LocalDate dataPal();
        Optional<String> numLot();
        String articleClient();
        String empresa();
        String nivellTecnic();
        String codiSeguretat();
        String aqptxt();
        long pesInicial10g();
        long pesFinal10g();
        @Default default List<MissatgeEntradaDetall> children() { return List.of(); }
        @Default default List<MissatgeEntradaRetornable> retornables() { return List.of(); }
        @Derived default String codiArticle() { return articleClient().substring(0,7); }
        @Derived default String codiClient() { return articleClient().substring(7); }
        @Derived default BigDecimal pesPremsatKg() { return Numbers.decimal(pesInicial10g())
                .divide(Numbers.decimal(10_000), 2, RoundingMode.HALF_UP);}
        @Derived default BigDecimal pesFinalKg() { return Numbers.decimal(pesFinal10g())
                .divide(Numbers.decimal(10_000), 2, RoundingMode.HALF_UP);}

        /**
         * En el format JSON quan s'envia un palet homogeni conté una llista (children) amb tots els detalls
         * de cada caixa que conté el palet homogeni.
         *
         * @return true quan es tracta d'un palet homogeni, false altrament
         */
        @Default default boolean isPaletHomogeni() {
            return !children().isEmpty();
        }

        /**
         * Quan es tracta d'un palet homogeni està informada la totalitat de peces del palet a l'atribut quantitatPalet
         * mentre que quan es informació d'una caixa dins d'un palet mixte o caixa suelta no ve informada l'atribut
         * quantitatPalet i ve a la quantitatCaixa
         *
         * @return quantitatPalet si està present, o quantitatCaixa altrament
         */
        @Default default long quantitat() {
            return quantitatPalet().orElse(quantitatCaixa());
        }

        /**
         * Quan es tracta d'un palet homogeni no està informat l'atribut numeroEtiqueta mentre que quan
         * és informació d'una caixa dins d'un palet mixte o caixa suelta si que ve informada el numeroEtiqueta
         *
         * @return numeroEtiqueta si està present, o numPalet altrament
         */
        @Default default long etiquetaCaixa() {
            return numeroEtiqueta().orElse(numPalet());
        }

    }

    @JsonDeserialize(builder = MissatgeEntradaDetallImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface MissatgeEntradaDetall {
        String tipus();
        long quantitatCaixa();
        long comanda();
        LocalDate dataLliurament();
        long numPalet();
        LocalDate dataEti();
        LocalDate dataPal();
        String numLot();
        long numeroEtiqueta();
        @Default default List<MissatgeEntradaRetornable> retornables() { return List.of(); }
    }

    @JsonDeserialize(builder = MissatgeEntradaRetornableImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface MissatgeEntradaRetornable {
        String tipus();
        long numPalet();
        Optional<Long> numeroEtiqueta();
        long quantitatComponent();
        String codiComponent();
        String nomComponent();
        String articleClient();
        @Derived default String codiArticle() { return articleClient().substring(0,7); }
        @Derived default String codiClient() { return articleClient().substring(7); }
    }

}
