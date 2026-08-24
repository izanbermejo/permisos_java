package ames.comercial.ofs.service;

import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.domain.service.AplicarNousTerminis;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CrearNovaOf {

    @Autowired OrdreFabricacioRepository ofRepo;

    private static final Map<String, Long> mapDiesIncrementComandes = Map.of(
            "01", 42L,
            "02", 42L,
            "05", 28L,
            "06", 28L,
            "07", 28L,
            "0A", 42L,
            "0B", 35L,
            "0D", 0L);

    public void executar(KeyArticleClient articleClient, String fabrica, List<Termini> terminis) {
        // Nou número d'OF
        var numOf = ofRepo.nextNumero();
        // Obtenció de l'of anterior
        var optOfAnterior = ofRepo.get(articleClient);
        if (optOfAnterior.isEmpty()) {
            // En cas que no hagi OF es crea una nova
            var novaOf = OrdreFabricacio.from(numOf, articleClient, fabrica, terminis);
            ofRepo.save(novaOf);
        } else {
            // En cas que si hi ha OF es tanca l'anterior i es
            // crea la nova apuntant a l'anterior calculant els terminis
            // resultants entre els anteriors i els nous
            var ofAnterior = optOfAnterior.get();
            var ofActualitzatPosterior = ofAnterior.tancar(numOf);
            var nousTerminis = new AplicarNousTerminis(ofAnterior.terminis(), terminis).executar();
            var diesIncrementComandes = mapDiesIncrementComandes.getOrDefault(fabrica, 0L);
            var novaOf = OrdreFabricacio.fromEspecial(ofAnterior, numOf, fabrica, nousTerminis, diesIncrementComandes);
            ofRepo.save(ofActualitzatPosterior);
            ofRepo.save(novaOf);
        }
    }

}
