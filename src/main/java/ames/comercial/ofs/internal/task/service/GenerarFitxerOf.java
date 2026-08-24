package ames.comercial.ofs.internal.task.service;

import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.ofs.internal.task.actions.TascaEnviamentOfsAction.ArtcliEnviamentOf;
import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.server.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
public class GenerarFitxerOf {

    @Value("${docsapps-folder}") private String pathDocsapps;
    @Autowired OrdreFabricacioRepository ofRepo;
    @Autowired IObtenirStocks obtenirStock;

    private static final DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter dataFormatterFitxer = DateTimeFormatter.ofPattern("yyMMdd");

    public void executar(List<ArtcliEnviamentOf> articlesClient, List<Long> numOfs, String prefix) {
        var contingutFitxer = new StringBuilder();
        numOfs.forEach(numOf -> {
            var of = ofRepo.get(numOf).orElseThrow();
            var article = articlesClient.stream()
                    .filter(a -> a.articleClient().equals(of.articleClient()))
                    .findAny().orElseThrow();
            contingutFitxer.append(generaTextOf(article, of));
        });
        guardarFitxer(prefix, contingutFitxer.toString());
    }

    private void guardarFitxer(String prefix, String contingut) {
        var nomFitxer = prefix + dataFormatterFitxer.format(LocalDate.now().minusDays(1)) + ".dos";
        Path pathOf = Paths.get(pathDocsapps + "/comercial/ofs/").resolve(nomFitxer);
        try {
            Files.write(pathOf, contingut.getBytes());
        } catch (IOException e) {
            throw new AppException(String.format( "Error al moure el fitxer d'entrades de magatzem repetit %s al directori %s", nomFitxer, pathOf));
        }
    }

    private String generaTextOf(ArtcliEnviamentOf artcli, OrdreFabricacio of) {
        var capsalera = generaCapsalera(artcli, of);
        var liniesTerminis = generaLiniesTerminis(of);
        return capsalera + liniesTerminis;
    }

    private String generaCapsalera(ArtcliEnviamentOf artcli, OrdreFabricacio of) {
        // Obtenció de l'stock
        var stock = obtenirStock.stockTotal(artcli.articleClient());
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(generaSeparador("C", 1, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(of.numero()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(artcli.clientNom(), 35, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(artcli.codiArticleClient(), 13, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(artcli.denominacio(), 36, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(artcli.referencia(), 18, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(of.ofAnteriorFitxerFabrica()), 8, Alineacio.DRETA));
        // 25/05/2026 Quan no hi ha data d'ultima quantitat rebuda s'envia un camp buit, abans s'enviava
        // la data 1899-12-30 que era la data que MS-Dos interpretava com a data null
        strBuilder.append(generaSeparador(of.dataUltimaQuantitatRebudaAbansCreacio().isEmpty()
                ? ""
                : dataFormatter.format(of.dataUltimaQuantitatRebudaAbansCreacio().get())
                , 8, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(of.quantitatRebudaEntradesAcumulatTotal()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(String.valueOf(of.ultimaQuantitatRebudaAbansCreacio()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(String.valueOf(of.quantitatTotal()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(dataFormatter.format(of.dataEmissio().minusDays(1)), 8, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(artcli.pesPremsatCentigrams()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(String.valueOf(artcli.pesFinalCentigrams()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(String.valueOf(stock), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(artcli.clientCodiProveidor().orElse(""), 14, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(artcli.preuMilesima()), 9, Alineacio.DRETA));
        strBuilder.append(generaSeparador(artcli.divisa(), 4, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(of.existeixIncrement() ? "S" : "N", 1, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(artcli.nivellTecnic(), 20, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(artcli.aclsts()), 8, Alineacio.DRETA));
        // Cal eliminar l'última coma i afegir un salt de línia
        var resultat = strBuilder.toString();
        return resultat.substring(0,resultat.length()-1) + "\n";
    }

    private String generaLiniesTerminis(OrdreFabricacio of) {
        StringBuilder result = new StringBuilder();
        // Ordenació dels terminis
        List<Termini> terminisOrdenats = of.terminis().stream()
                .sorted(
                        Comparator.comparing(Termini::data) // Ordenació per data
                                .thenComparing(Termini::isStockSeguretat) // En cas de la mateixa data primer las de client
                                .thenComparing(Comparator.comparing(Termini::quantitat).reversed()) // En cas igual les dos condicions anteriors per quantitat ascendent
                )
                .toList();
        // Recorregut dels terminis
        terminisOrdenats.forEach(t -> {
            result.append(generaLiniaTermini(of.numero(), t));
        });
        return result.toString();
    }

    private String generaLiniaTermini(long numeroOf, Termini term) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(generaSeparador("L", 1, Alineacio.ESQUERRA));
        strBuilder.append(generaSeparador(String.valueOf(numeroOf), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(dataFormatter.format(term.data()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(String.valueOf(term.quantitatAnterior()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(String.valueOf(term.quantitat()), 8, Alineacio.DRETA));
        strBuilder.append(generaSeparador(dataFormatter.format(term.dataSortida()), 8, Alineacio.DRETA));
        // 27/10/2025 A la posada en marxa del nou càlcul d'OFS ens comenten
        // els companys de fàbrica que no enviem la quantitat d'stock de seguretat
        // ja que no saben si pot produir un error en el seu sistema (MS-DOS)
        //var qtatSeguretat = term.isStockSeguretat() ? term.quantitat() : 0;
        //strBuilder.append(generaSeparador(String.valueOf(qtatSeguretat), 8, Alineacio.DRETA));
        // Cal eliminar l'última coma i afegir un salt de línia
        var resultat = strBuilder.toString();
        return resultat.substring(0,resultat.length()-1) + "\n";
    }

    private String generaSeparador(String valor, int longitud, Alineacio alineacio) {
        // Es retalla el contingut si supera la longitud indicada
        var contingutFinal = valor.length() > longitud ? valor.substring(0, longitud) : valor;
        // Es substitueixen les " per ' ja que el fitxer porta " com a separador
        // i si algun nom porta les " podria generar errors en la recepció a fàbrica
        contingutFinal = contingutFinal.replace("\"", "'");
        // Espais restants per omplir
        var espaiRestant = Math.max(longitud - contingutFinal.length(), 0);
        // S'omple d'espais a la dreta o esquerra en funció de l'alineació
        if (Alineacio.ESQUERRA.equals(alineacio)) {
            contingutFinal = contingutFinal + " ".repeat(espaiRestant);
        } else {
            contingutFinal = " ".repeat(espaiRestant) + contingutFinal;
        }
        return "\"" + contingutFinal + "\",";
    }

    private enum Alineacio {
        ESQUERRA,
        DRETA;
    }

}
