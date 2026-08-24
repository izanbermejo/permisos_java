package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.IObtenirNumeradorAlbara;
import ames.comercial.albarans.internal.domain.albara.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.InformacioEnviament;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CrearCapsaleraAlbara {

    IObtenirNumeradorAlbara obtenirNumeradorAlbara;

    public CrearCapsaleraAlbara (IObtenirNumeradorAlbara obtenirNumeradorAlbara) {
        this.obtenirNumeradorAlbara = obtenirNumeradorAlbara;
    }

    public Albara executar(Empresa empresa, Adresa adresa, InformacioEnviament informacioEnviament, String client,
                           LocalDate dataAlbara, String magatzem, boolean isFacturacioAutomatica, boolean isTancat) {
        // Obtenició del número d'albarà
        var numeroAlbara = obtenirNumeradorAlbara.obtenir(empresa.clau());
        return AlbaraImpl.builder()
                .id(KeyAlbara.of(numeroAlbara, empresa.clau()))
                .tipus(TipusAlbara.CLIENT)
                .client(client)
                .data(dataAlbara)
                .magatzem(magatzem)
                .adresa(adresa)
                .informacioEnviament(informacioEnviament)
                .informacioMagatzem(InformacioMagatzem.empty())
                .dataCreacio(LocalDate.now())
                .usuariCreacio(RequestThread.nomUsuari())
                .isFacturacioAutomatica(isFacturacioAutomatica)
                .isTancat(isTancat)
                .build();
    }

}
