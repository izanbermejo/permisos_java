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
public class CrearCapsaleraAlbaraTraspas {

    IObtenirNumeradorAlbara obtenirNumeradorAlbara;

    public CrearCapsaleraAlbaraTraspas(IObtenirNumeradorAlbara obtenirNumeradorAlbara) {
        this.obtenirNumeradorAlbara = obtenirNumeradorAlbara;
    }

    public Albara executar(Empresa empresa, Adresa adresa, InformacioEnviament informacioEnviament, LocalDate dataAlbara, String magatzem,
                           String magatzemReceptor, Empresa empresaReceptora, boolean isTancat) {
        return executar(empresa, adresa, informacioEnviament, dataAlbara, magatzem, magatzemReceptor, empresaReceptora, isTancat, false);
    }

    /**
     * @param isTraspasAbonable cert si el traspàs s'abona en comptes de facturar-se. Només té sentit amb
     *                          canvi d'empresa; qui ho decideix és qui crida (a la creació automàtica des
     *                          de la proposta sempre és fals).
     */
    public Albara executar(Empresa empresa, Adresa adresa, InformacioEnviament informacioEnviament, LocalDate dataAlbara, String magatzem,
                           String magatzemReceptor, Empresa empresaReceptora, boolean isTancat, boolean isTraspasAbonable) {
        // Obtenició del número d'albarà
        var numeroAlbara = obtenirNumeradorAlbara.obtenir(empresa.clau());
        var tipusAlbara = tipusAlbara(empresa, empresaReceptora, magatzem, magatzemReceptor);
        return AlbaraImpl.builder()
                .id(KeyAlbara.of(numeroAlbara, empresa.clau()))
                .tipus(tipusAlbara)
                .data(dataAlbara)
                .magatzem(magatzem)
                .adresa(adresa)
                .informacioEnviament(informacioEnviament)
                .informacioMagatzem(InformacioMagatzem.empty())
                .informacioTraspas(buildInformacioTraspas(empresaReceptora.clau(), magatzemReceptor, isTraspasAbonable))
                .dataCreacio(LocalDate.now())
                .usuariCreacio(RequestThread.nomUsuari())
                .isFacturacioAutomatica(false) // TODO Obtenir-ho del client
                .isTancat(isTancat)
                .build();
    }

    private TipusAlbara tipusAlbara(Empresa empresaOrigen, Empresa empresaReceptora, String magatzemOrigen, String magatzemReceptor) {
        var traspasEmpresa = isTraspasEmpresa(empresaOrigen, empresaReceptora);
        var traspasMagatzem = isTraspasMagatzem(magatzemOrigen, magatzemReceptor);
        if (traspasEmpresa && traspasMagatzem) {
            return TipusAlbara.TRASPAS_MAGATZEM_EMPRESA;
        } else if (traspasEmpresa) {
            return TipusAlbara.TRASPAS_EMPRESA;
        }
        return TipusAlbara.TRASPAS_MAGATZEM;
    }

    private boolean isTraspasEmpresa(Empresa empresaOrigen, Empresa empresaReceptora) {
        return !empresaOrigen.clau().equals(empresaReceptora.clau());
    }

    private boolean isTraspasMagatzem(String magatzemOrigen, String magatzemReceptor) {
        return !magatzemOrigen.equals(magatzemReceptor);
    }

    private InformacioTraspas buildInformacioTraspas(String empresaReceptora, String magatzemReceptor, boolean traspasAbonable) {
        return InformacioTraspasImpl.builder()
                .empresaReceptora(empresaReceptora)
                .magatzemReceptor(magatzemReceptor)
                .isTraspasAbonable(traspasAbonable)
                .build();
    }

}
