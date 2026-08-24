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
public class CrearCapsaleraAlbaraTraspasPlataforma {

    IObtenirNumeradorAlbara obtenirNumeradorAlbara;

    public CrearCapsaleraAlbaraTraspasPlataforma(IObtenirNumeradorAlbara obtenirNumeradorAlbara) {
        this.obtenirNumeradorAlbara = obtenirNumeradorAlbara;
    }

    public Albara executar(Empresa empresa, Adresa adresa, InformacioEnviament informacioEnviament, LocalDate dataAlbara, String magatzem,
                           String magatzemReceptor, boolean isTancat) {
        // Obtenició del número d'albarà
        var numeroAlbara = obtenirNumeradorAlbara.obtenir(empresa.clau());
        return AlbaraImpl.builder()
                .id(KeyAlbara.of(numeroAlbara, empresa.clau()))
                .tipus(TipusAlbara.TRASPAS_PLATAFORMA)
                .data(dataAlbara)
                .magatzem(magatzem)
                .adresa(adresa)
                .informacioEnviament(informacioEnviament)
                .informacioMagatzem(InformacioMagatzem.empty())
                .informacioTraspas(buildInformacioTraspas(empresa.clau(), magatzemReceptor, false))
                .dataCreacio(LocalDate.now())
                .usuariCreacio(RequestThread.nomUsuari())
                .isFacturacioAutomatica(false) // TODO Obtenir-ho del client
                .isTancat(isTancat)
                .build();
    }

    private InformacioTraspas buildInformacioTraspas(String empresaReceptora, String magatzemReceptor, boolean traspasAbonable) {
        return InformacioTraspasImpl.builder()
                .empresaReceptora(empresaReceptora)
                .magatzemReceptor(magatzemReceptor)
                .isTraspasAbonable(traspasAbonable)
                .build();
    }

}
