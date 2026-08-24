package ames.comercial.albarans.internal.services;

import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.advantage.IObtenirNumeradorAlbara;
import ames.comercial.albarans.internal.domain.albara.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.SharedExceptions.MagatzemNoExisteix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CrearCapsaleraAlbaraConsum {

    @Autowired IObtenirNumeradorAlbara obtenirNumeradorAlbara;
    @Autowired IObtenirMagatzems obtenirMagatzemAds;

    /**
     * Crea la capçalera d'un albarà de consum ({@code TipusAlbara.CONSUM}). El client i l'empresa els
     * tria l'usuari en començar el consum (entre els que tenen stock a la plataforma) i el magatzem és
     * el magatzem plataforma. L'adreça i la informació d'enviament s'obtenen del magatzem plataforma
     * (com en els traspassos a plataforma).
     */
    public Albara executar(Empresa empresa, String client, LocalDate dataAlbara, String magatzemPlataforma,
                           Optional<String> albaraEspecial, boolean isFacturacioAutomatica) {
        var numeroAlbara = obtenirNumeradorAlbara.obtenir(empresa.clau());
        var magatzemAds = obtenirMagatzemAds.get(magatzemPlataforma)
                .orElseThrow(() -> new MagatzemNoExisteix(magatzemPlataforma));

        return AlbaraImpl.builder()
                .id(KeyAlbara.of(numeroAlbara, empresa.clau()))
                .tipus(TipusAlbara.CONSUM)
                .client(client)
                .data(dataAlbara)
                .magatzem(magatzemPlataforma)
                .adresa(magatzemAds.adresa())
                .informacioEnviament(InformacioEnviament.desconegut())
                .informacioMagatzem(InformacioMagatzem.empty())
                .numeroAlbaraEspecial(albaraEspecial)
                .dataCreacio(LocalDate.now())
                .usuariCreacio(RequestThread.nomUsuari())
                .isFacturacioAutomatica(isFacturacioAutomatica)
                // El consum es crea obert perquè es pugui anar construint (afegint línies, adjunts...);
                // l'usuari el tanca quan acaba, moment en què passa a ser facturable
                .isTancat(false)
                .build();
    }

}
