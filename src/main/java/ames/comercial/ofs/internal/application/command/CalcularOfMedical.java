package ames.comercial.ofs.internal.application.command;

import ames.comercial.comandes.ext.ObtenirQuantitatPendentServir;
import ames.comercial.comandes.service.IProviderStocks;
import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsMedicalAction.ArtcliMedical;
import ames.comercial.ofs.service.CrearNovaOf;
import ames.comercial.ofs.service.ObtenirPeriodeFabricacioEstandardNormalitzat;
import ames.comercial.ofs.service.ObtenirQuantitatPendentRebreOf;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
public class CalcularOfMedical {

    private static final String MAGATZEM_MEDICAL = "0045";

    @Autowired IProviderStocks providerStocks;
    @Autowired ObtenirQuantitatPendentRebreOf quantitatPendentRebreOf;
    @Autowired ObtenirQuantitatPendentServir quantitatPendentServir;
    @Autowired OrdreFabricacioRepository ofRepository;
    @Autowired ObtenirPeriodeFabricacioEstandardNormalitzat obtenirPeriodeFabricacioEstandardNormalitzat;
    @Autowired CrearNovaOf crearNovaOf;

    @Transactional
    public void executar(ArtcliMedical article) {
        var articleClient = KeyArticleClient.ofNormalitzat(article.artint());
        // Obtenció de l'stock pel magatzem 0045 de MEDICAL
        var stock = providerStocks.provide(articleClient, MAGATZEM_MEDICAL);
        // Obtenció de la quantita pendent de rebre
        var qtatPendentRebre = quantitatPendentRebreOf.executar(articleClient);
        // Obtenció de la quantitat pendent de servir
        var qtatPendentServir = quantitatPendentServir.executar(articleClient, Empresa.MEDICAL);

        // L'stock futur és l'stock actual més la quantitat pendent de rebre i restar
        // la quantitat pendent de servir
        var stockFutur = stock + qtatPendentRebre - qtatPendentServir;

        // En cas que l'stock futur quedi per sota del mínim caldrà fer OF
        if (stockFutur < article.stockMinim()) {
            // La quantitat per arribar al mínim es la diferència entre l'stock mínim i el futur
            long quantitatPerArribarMinim = Math.max(0, article.stockMinim() - stockFutur);
            // Càlcul de la quantitat a fabricar tenint en compte que s'ha de fabricar en
            // múltiples del lot mínim
            long quantitatCalculada = calcularQuantitatFabricar(quantitatPerArribarMinim, article.lotMinim());
            // Càlcul dels nous terminis
            var nousTerminis = calculaTerminis(articleClient, quantitatCalculada);
            // Creació de la nova OF
            crearNovaOf.executar(articleClient, article.codiFabrica(), nousTerminis);
        }
    }

    private long calcularQuantitatFabricar(long quantitatFabricar, long lot) {
        var bdQtatFabricar = BigDecimal.valueOf(quantitatFabricar);
        var bdLot = BigDecimal.valueOf(lot);
        var diviso = bdQtatFabricar.divide(bdLot, 0, RoundingMode.CEILING); // Arrodoneix cap amunt
        return diviso.multiply(bdLot).longValue();
    }

    private List<Termini> calculaTerminis (KeyArticleClient articleClient, long novaQuantitat) {
        var dataFabricacio = LocalDate.now().plusDays(obtenirPeriodeFabricacioEstandardNormalitzat.executar()-1);
        var nouTermini = Termini.nou(novaQuantitat, dataFabricacio);

        // Es retornen els terminis anteriors que no estan pendents amb el nou termini afegit
        var resultat = new ArrayList<>(terminisActuals(articleClient).stream()
                .filter(Termini::isPendent)
                .map(Termini::nouAnterior)
                .toList());
        resultat.add(nouTermini);
        return resultat;
    }

    private List<Termini> terminisActuals (KeyArticleClient articleClient) {
        return ofRepository.get(articleClient)
                .filter(Predicate.not(OrdreFabricacio::isAnulada))
                .map(OrdreFabricacio::terminis)
                .orElse(List.of());
    }

}
