package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.IObtenirEmpresesAds;
import ames.comercial.advantage.IObtenirEtiquetesAlbara;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.aviexp.GenerarBlocLinia;
import ames.comercial.albarans.internal.services.aviexp.GenerarFitxerAviexp;
import ames.comercial.albarans.internal.services.aviexp.GenerarZZ;
import ames.comercial.aviexp.internal.services.agrupacio.AgrupaAviExp;
import ames.comercial.comandes.ext.IObtenirClausComandesEdi;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.edi2.internal.application.ObtenirComandaEdi2;
import ames.comercial.edi2.internal.infraestructure.capsalera.CapsaleraEdiRepository;
import ames.comercial.inventari.internal.application.query.ObtenirClauLiniesComandaLiniesAlbara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EnviarAviexp {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired GenerarFitxerAviexp generarCapsalera;
    @Autowired GenerarBlocLinia generarBlocLinia;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired IObtenirEtiquetesAlbara obtenirEtiquetesAlbara;
    @Autowired ObtenirClauLiniesComandaLiniesAlbara obtenirClauLiniesComandaLiniesAlbara;
    @Autowired IObtenirClausComandesEdi obtenirClausComandesEdi;
    @Autowired IObtenirEmpresesAds obtenirEmpresesAds;
    @Autowired ObtenirComandaEdi2 obtenirComandaEdi;
    // TODO Amb el servei de capçalera
    @Autowired CapsaleraEdiRepository capsaleraEdiRepository;

    public String executar(KeyAlbara keyAlbara) {
        // Recuperació de l'albarà i les seves línies
        var albara = albaraRepository.find(keyAlbara).orElseThrow(() -> new AlbaraNoExisteix(keyAlbara));
        var liniesAlbara = liniaAlbaraRepository.findByAlbara(keyAlbara);

        // Obtenció de l'empresa de l'albarà
        var empresa = obtenirEmpresesAds.get(albara.id().empresa()).orElseThrow();

        // Obtenció de les etiquetes de transport associades a l'albarà
        var etiquetesAlbara = obtenirEtiquetesAlbara.executar(keyAlbara);
        // Agrupació de les línies de l'albarà i les etiquetes de transport associades per articleClient i comandaClient
        var agrupacioAviExp = new AgrupaAviExp(liniesAlbara, etiquetesAlbara).agrupar();

        // Obtenció de les claus de línies de comanda associades a les línies de l'albarà a través dels moviments d'inventari,
        // per assegurar que es tenen en compte totes les línies de comanda EDI associades a l'albarà,
        // independentment de l'ordre de les línies a l'albarà
        var liniesComanda = obtenirComandaEdiCapsalera(liniesAlbara);
        System.out.println("Línies de comanda associades a les línies de l'albarà:");
        liniesComanda.forEach(liniaComanda -> System.out.println("- " + liniaComanda));

        // Obtenció de les claus de comanda EDI associades a les línies de comanda obtingudes
        var clausComandaEdi = obtenirClausComandesEdi.executar(liniesComanda);
        System.out.println("Claus de comanda EDI associades a les línies de comanda:");
        clausComandaEdi.forEach((liniaComanda, comandaEdi) -> System.out.println("- " + liniaComanda + " -> " + comandaEdi));

        // Obtenció d'una comanda EDI (en cas que no hagi cap no es pot generar el fitxer AviExp, per tant es llença una excepció)
        if (clausComandaEdi.isEmpty())
            throw new RuntimeException("No s'ha trobat cap comanda EDI associada a les línies de comanda de l'albarà");     // TODO Missatge d'excepció més específic
        var clauComandaEdi = clausComandaEdi.entrySet().stream().findFirst().get().getValue().keyComandaEdi();
        var comandaEdi = obtenirComandaEdi.executar(clauComandaEdi).orElseThrow();                                          // TODO Missatge d'excepció més específic
        var capsaleraEdi = capsaleraEdiRepository.obtenir(comandaEdi.idMissatge(), comandaEdi.idCapsalera()).orElseThrow(); // TODO Missatge d'excepció més específic

        System.out.println("Agrupació de les línies de l'albarà i les etiquetes de transport associades per articleClient i comandaClient:");
        System.out.println(agrupacioAviExp);

        var sb = new StringBuilder();
        // Capçalera del fitxer
        sb.append(generarCapsalera.generar(albara, capsaleraEdi, comandaEdi, empresa));

        // Línies del fitxer
        for (var entryAgrupacio : agrupacioAviExp.entrySet()) {
            // Clau agrupació: parella articleClient-comandaClient
            var clauAgrupacio = entryAgrupacio.getKey();
            var agrupacio = entryAgrupacio.getValue();
            sb.append(generarBlocLinia.executar(empresa, clauAgrupacio.articleClient(), albara.id(), agrupacio,
                    clausComandaEdi.get(clauAgrupacio), capsaleraEdi.tipoMissatge()));
        }

        // Tancament del fitxer
        String textAviexp = sb.toString();
        int numRegistres = textAviexp.split("\\R").length;

        // S'afegeix el registre ZZ
        textAviexp += new GenerarZZ(numRegistres).generar();

        return textAviexp;
    }

    private Set<KeyLiniaComanda> obtenirComandaEdiCapsalera(List<LiniaAlbara> liniesAlbara) {
        // Ordenació de les línies de l'albarà per número de línia (en ordre ascendent)
        var liniesOrdenades = liniesAlbara.stream()
                .sorted(Comparator.comparingLong(l -> l.id().linia()))
                .toList();

        // Conjunt per emmagatzemar les línies de comanda EDI associades a les línies de l'albarà, evitant duplicats
        var resultat = new HashSet<KeyLiniaComanda>();
        // Iteració sobre les línies ordenades per obtenir la primera línia de comanda EDI associada
        for (var linia : liniesOrdenades) {
            // Per cada línia de l'albarà, s'obté la llista de línies de comanda associades a través dels moviments d'inventari
            // i s'afegeix al conjunt de resultat
            resultat.addAll(new HashSet<>(obtenirClauLiniesComandaLiniesAlbara.executar(linia.id())));
        }
        return resultat;
    }

}
