package ames.comercial.clients.internal.application.query;

import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ObtenirNomFitxerDisponibleClient {

    @Autowired
    AdjuntClientRepositoryDatabase adjuntComandaRepo;

    public String executar (String codiClient, String nomFitxerOriginal) {
        int indexPunt = nomFitxerOriginal.lastIndexOf('.');
        String nomBase = indexPunt != -1 ? nomFitxerOriginal.substring(0, indexPunt) : nomFitxerOriginal;
        String extensio = indexPunt != -1 ? nomFitxerOriginal.substring(indexPunt) : "";

        // Obtenció dels noms similars de BBDD
        Set<String> nomsExistents = new HashSet<>(adjuntComandaRepo.findNomSimilar(codiClient, nomBase));

        // En cas que no existeixi el nom original, es fa servir directament
        if (!nomsExistents.contains(nomFitxerOriginal))
            return nomFitxerOriginal;

        // Buscar el màxim número ja utilitzat
        int maxNumero = 0;
        Pattern pattern = Pattern.compile(Pattern.quote(nomBase) + "_(\\d+)" + Pattern.quote(extensio));

        for (String nom : nomsExistents) {
            Matcher matcher = pattern.matcher(nom);
            if (matcher.matches()) {
                int numero = Integer.parseInt(matcher.group(1));
                if (numero > maxNumero) {
                    maxNumero = numero;
                }
            }
        }

        // Generar el nou nom amb el número següent
        return nomBase + "_" + (maxNumero + 1) + extensio;
    }

}
