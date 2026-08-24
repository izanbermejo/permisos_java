package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.infraestructure.ConfigFabricaEntradesRepository;
import ames.comercial.server.exception.AppException;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;
import ames.comercial.entrades.internal.domain.EntradaMagatzemDetallImpl;
import ames.comercial.entrades.internal.domain.EntradaMagatzemEmbalatgeImpl;
import ames.comercial.entrades.internal.domain.EntradaMagatzemImpl;
import ames.comercial.entrades.internal.domain.EntradaMagatzemImpl.Builder;
import ames.comercial.shared.Numbers;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntradaMagatzemParserFitxer {

    ConfigFabricaEntradesRepository configFabricaRepo;
    String idEntradaFabrica;

    public EntradaMagatzemParserFitxer(ConfigFabricaEntradesRepository configFabricaRepo) {
        this.configFabricaRepo = configFabricaRepo;
    }

    public List<EntradaMagatzem> parse (String idEntradaFabrica, String contingut) {
        this.idEntradaFabrica = idEntradaFabrica;
        // El fitxer està separat per salts de línia
        var listLinies = Arrays.asList(contingut.split("\n"));
        return parseLinies(listLinies.stream());
    }

    private List<EntradaMagatzem> parseLinies(Stream<String> linies) {
        String magatzemEntrada = "";
        List<EntradaMagatzem> result = new ArrayList<>();
        Builder builder = null;
        List<EntradaMagatzemDetall> detalls = new ArrayList<>();
        List<EntradaMagatzemEmbalatge> embalatges = new ArrayList<>();
        // Recorregut de cada línia
        for (var it = linies.iterator(); it.hasNext();) {
            var linia = it.next();
            // En la primera línea s'obté el codi de fàbrica i a partir d'aquesta
            // s'obté el magatzem d'entrada
            if (magatzemEntrada.isBlank()) {
                var fabcod = linia.substring(29,29+2).trim();
                magatzemEntrada = obtenirMagatzemEntrada(fabcod);
            }
            // Cas Capçalera
            if (linia.startsWith("C")) {
                if (builder != null) {
                    builder.detalls(detalls);
                    builder.embalatges(embalatges);
                    result.add(builder.build());
                }
                builder = builderEntradaMagatzem(linia, magatzemEntrada);
                detalls = new ArrayList<>();
                embalatges = new ArrayList<>();
            } else if (linia.startsWith("D")) {
                // Cas detall
                detalls.add(parseDetall(linia));
            } else if (linia.startsWith("E")) {
                // Cas embalatge
                embalatges.add(parseEmbalatge(linia));
            }
        }
        // S'agrega l'últim registre si existeix
        if (builder != null) {
            builder.detalls(detalls);
            builder.embalatges(embalatges);
            result.add(builder.build());
        }
        return result;
    }

    private String obtenirMagatzemEntrada(String codiFabrica) {
        return configFabricaRepo.get(codiFabrica)
                .map(c -> c.magatzem())
                .orElseThrow(() -> new AppException(
                        String.format("Error parsejant fitxer entrades. Fàbrica amb codi %s no trobada a la configuració d'entrades (entrades.config_fabrica)", codiFabrica)));
    }

    private Builder builderEntradaMagatzem(String linia, String magatzem) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var aclfab = linia.substring(1, 1+7).trim();
        var clicod = linia.substring(8, 8+6).trim();
        var fabcod = linia.substring(29,29+2).trim();
        var cantot = Long.parseLong(linia.substring(35, 35+9).trim());
        var piecaj = Long.parseLong(linia.substring(44, 44+9).trim());
        var dateti = LocalDate.parse(linia.substring(62, 62+10).trim(), formatter);
        var lote   = linia.substring(72, 72+9).trim();
        var of     = Long.parseLong(linia.substring(81, 81+8).trim());
        var eticaj = Long.parseLong(linia.substring(89, 89+12).trim());
        var nivtec = linia.substring(101,101+14).trim();
        var codseg = linia.substring(115,115+1).trim();
        var codcal = linia.substring(116,116+4).trim();
        var datent = LocalDate.parse(linia.substring(120, 120+10).trim(), formatter);
        var pespreGrams = Long.parseLong(linia.substring(130,130+9).trim());
        var pespreKg = Numbers.decimal(pespreGrams)
                .divide(Numbers.decimal(10_000), 2, RoundingMode.HALF_UP);
        var pesfinGrams = Long.parseLong(linia.substring(139,139+9).trim());
        var pesfinKg = Numbers.decimal(pesfinGrams)
                .divide(Numbers.decimal(10_000), 2, RoundingMode.HALF_UP);
        var etipal = Long.parseLong(linia.substring(148,148+12).trim());
        return EntradaMagatzemImpl.builder()
                .idEntradaFabrica(idEntradaFabrica)
                .articleFabrica(aclfab)
                .client(clicod)
                .magatzem(magatzem)
                .fabrica(fabcod)
                .quantitat(cantot)
                .quantitatCaixa(piecaj)
                .dataEtiqueta(dateti)
                .lot(lote)
                .of(of)
                .etiquetaCaixa(eticaj)
                .etiquetaPalet(etipal)
                .nivellTecnic(nivtec)
                .codiSeguretat(codseg)
                .codiCal(codcal)
                .dataEntrada(datent)
                .pesPremsat(pespreKg)
                .pesFinal(pesfinKg);
    }

    private EntradaMagatzemDetall parseDetall (String linia) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var cantot = Long.parseLong(linia.substring(5, 5+9).trim());
        var dateti = LocalDate.parse(linia.substring(14, 14+10).trim(), formatter);
        var lote   = linia.substring(24, 24+9).trim();
        var of     = Long.parseLong(linia.substring(33, 33+8).trim());
        var eticaj = Long.parseLong(linia.substring(41, 41+12).trim());
        return EntradaMagatzemDetallImpl.builder()
                .etiquetaCaixa(eticaj)
                .quantitat(cantot)
                .dataEtiqueta(dateti)
                .lot(lote)
                .of(of)
                .build();
    }

    private EntradaMagatzemEmbalatge parseEmbalatge (String linia) {
        var article = linia.substring(1, 1+7).trim();
        var client = linia.substring(8, 8+6).trim();
        var codiElement = linia.substring(38, 38+8).trim();
        var strEtiqCaixa = linia.substring(14, 14+12).trim();
        Optional<Long> etiqCaixa = strEtiqCaixa.isBlank() ? Optional.empty() : Optional.of(Long.valueOf(strEtiqCaixa));
        var etiqPalet = Long.parseLong(linia.substring(26, 26+12).trim());
        var descripcio = linia.substring(46, 46+50).trim();
        var quantitat = Long.parseLong(linia.substring(96, 96+9).trim());
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

}
