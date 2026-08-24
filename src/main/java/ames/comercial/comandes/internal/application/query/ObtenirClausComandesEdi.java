package ames.comercial.comandes.internal.application.query;

import ames.comercial.aviexp.internal.services.agrupacio.ClauAgrupacioAviExp;
import ames.comercial.comandes.ext.IObtenirClausComandesEdi;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.informacioedi.InformacioEdiRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ObtenirClausComandesEdi implements IObtenirClausComandesEdi {

    @Autowired LiniaComandaRepository liniaComandaRepository;
    @Autowired ComandaRepository comandaRepository;
    @Autowired InformacioEdiRepository informacioEdiRepository;

    @Override
    public Map<ClauAgrupacioAviExp, DataSolicitadaComandaEdi> executar(Iterable<KeyLiniaComanda> clausLiniesComanda) {
        var map = new HashMap<ClauAgrupacioAviExp, DataSolicitadaComandaEdi>();
        // S'ha de buscar per cada línia de comanda, la comanda a la que pertany, i després buscar la informació EDI
        for (var keyLinia : clausLiniesComanda) {

            var optLiniaComanda = liniaComandaRepository.find(keyLinia);
            // Si no es troba la línia de comanda, es continua amb la següent iteració
            if (optLiniaComanda.isEmpty()) continue;
            var liniaComanda = optLiniaComanda.get();

            // Obtenció de la comanda
            var optComanda = comandaRepository.find(liniaComanda.comanda());
            if (optComanda.isEmpty()) continue;
            var comanda = optComanda.get();

            // Cerca de la informació EDI associada a la línia de comanda, a través de les dades de la comanda i la línia de comanda
            var optKeyComandaEdi = informacioEdiRepository.findKeyComandaEdi(
                    comanda.informacioClient().identificador(),
                    liniaComanda.dataSolicitada(),
                    liniaComanda.articleClient()
            );

            // Si es troba la informació EDI, s'associa la clau de línia de comanda amb la clau de comanda EDI al mapa de resultats
            if (optKeyComandaEdi.isEmpty()) continue;
            var dataSolicitadaComandaEdi = new DataSolicitadaComandaEdi(liniaComanda.dataSolicitada(), optKeyComandaEdi.get());
            var clauAgrupacio = new ClauAgrupacioAviExp(liniaComanda.articleClient(), comanda.informacioClient().identificador());
            map.put(clauAgrupacio, dataSolicitadaComandaEdi);

        }
        return map;
    }

}
