package ames.comercial.albarans.internal.application.query;

import ames.comercial.advantage.internal.ObtenirPesFinalAds;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class ObtenirPesNetAlbara {

    @Autowired private LiniaAlbaraRepository liniaAlbaraRepository;

    public BigDecimal executar(KeyAlbara keyAlbara) {
        // Obtenció de les línies de l'albarà
        var linies = liniaAlbaraRepository.findByAlbara(keyAlbara);
        // Obtenció dels codis d'article de les línies
        var articles = linies.stream()
                .map(l -> l.articleClient().artint())
                .collect(Collectors.toSet());
        // Obtenció del pes final dels articles
        var pesos = new ObtenirPesFinalAds().query(articles);
        // Càlcul del pes net de l'albarà sumant el pes final de cada línia multiplicat per la quantitat de la línia
        return linies.stream()
                .map(l -> {
                    var pesFinal = pesos.getOrDefault(l.articleClient().artint(), BigDecimal.ZERO);
                    return pesFinal.multiply(BigDecimal.valueOf(l.quantitat()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
