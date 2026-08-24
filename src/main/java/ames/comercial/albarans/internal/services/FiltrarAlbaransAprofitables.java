package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO.LiniaAlbaraDTO;
import ames.comercial.albarans.internal.services.agrupacio.ClauAgrupacio;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FiltrarAlbaransAprofitables {

    List<AlbaraAmbLiniesDTO> albaransOberts;
    ClauAgrupacio clauAgrupacio;
    LocalDate dataObjectiu;

    public FiltrarAlbaransAprofitables(List<AlbaraAmbLiniesDTO> albaransOberts, ClauAgrupacio clauAgrupacio, LocalDate dataObjectiu) {
        this.albaransOberts = albaransOberts;
        this.clauAgrupacio = clauAgrupacio;
        this.dataObjectiu = dataObjectiu;
    }

    public List<AlbaraAmbLiniesDTO> executar(InformacioLiniaComandaDTO liniaComanda) {
        return albaransOberts.stream()
                .filter(this::coincideixData)                   // Només s'aprofiten els albarans oberts de la data objectiu
                .filter(this::coincideixEmpresa)                // Ha de coincidir l'empresa
                .filter(this::coincideixAdresaEnviament)        // Ha de coincidir l'adreça d'enviament
                .filter(this::coincideixInformacioEnviament)    // Ha de coincidir la informació d'enviament
                .filter(alb -> compleixModeAgrupacio(alb, liniaComanda))   // Ha de complir el mode d'agrupació (segons el mode d'agrupació del client, pot ser necessari que coincideixi la comanda i/o la pesa)
                .toList();
    }

    private boolean coincideixData(AlbaraAmbLiniesDTO albara) {
        return albara.data().equals(dataObjectiu);
    }

    private boolean coincideixEmpresa(AlbaraAmbLiniesDTO albara) {
       return albara.idAlbara().empresa().equals(clauAgrupacio.empresa());
    }

    private boolean coincideixAdresaEnviament(AlbaraAmbLiniesDTO albara) {
        return albara.adresa().equals(clauAgrupacio.adresa());
    }

    private boolean coincideixInformacioEnviament(AlbaraAmbLiniesDTO albara) {
        return albara.informacioEnviament().equals(clauAgrupacio.informacioEnviament());
    }

    private boolean compleixModeAgrupacio(AlbaraAmbLiniesDTO albara, InformacioLiniaComandaDTO liniaComanda) {
        return switch (clauAgrupacio.tipus()) {
            // Si el mode d'agrupació és AGRUPAR_TOT, es poden aprofitar tots els albarans
            // oberts que coincideixin en empresa, adreça d'enviament i informació d'enviament
            case AGRUPAR_TOT -> true;

            // Si el mode d'agrupació és AGRUPAR_PER_COMANDA, només es poden aprofitar les línies d'albarans oberts que coincideixin
            // en comanda amb la línia de comanda a servir (segons el codi de comanda que té la línia de comanda segons el client)
            case AGRUPAR_PER_COMANDA -> coincideixComanda(albara, liniaComanda);

            // Si el mode d'agrupació és AGRUPAR_PER_PESA, només es poden aprofitar les línies d'albarans oberts que coincideixin
            // en pesa amb la línia de comanda a servir
            case AGRUPAR_PER_PESA -> coincideixPesa(albara, liniaComanda);

            // Si el mode d'agrupació és AGRUPAR_PER_COMANDA_PESA, només es poden aprofitar les línies d'albarans oberts que coincideixin
            // en comanda i en peça amb la línia de comanda a servir
            case AGRUPAR_PER_COMANDA_PESA -> coincideixComanda(albara, liniaComanda) && coincideixPesa(albara, liniaComanda);

        };
    }

    private boolean coincideixComanda(AlbaraAmbLiniesDTO albara, InformacioLiniaComandaDTO liniaComanda) {
        return albara.linies().stream()
                .map(LiniaAlbaraDTO::infoComanda)
                .flatMap(Optional::stream) // Filtre de les línies d'albarà que tenen informació de comanda associada
                .allMatch(infoComanda -> infoComanda.comandaClient().equals(liniaComanda.comandaSegonsClient()));
    }

    private boolean coincideixPesa(AlbaraAmbLiniesDTO albara, InformacioLiniaComandaDTO liniaComanda) {
        return albara.linies().stream()
                .allMatch(linia -> linia.articleClient().equals(liniaComanda.articleClient()));
    }

}
