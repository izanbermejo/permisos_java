package ames.comercial.albarans.internal.services.agrupacio;

import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.KeyArticleClient;

import static ames.comercial.albarans.internal.services.agrupacio.ModeAgrupacio.*;

public record ClauAgrupacio(String comanda, KeyArticleClient articleClient, String empresa, Adresa adresa, InformacioEnviament informacioEnviament) {

    public static ClauAgrupacio tot(String empresa, Adresa adresa, InformacioEnviament informacioEnviament) {
        return new ClauAgrupacio(null, null, empresa, adresa, informacioEnviament);
    }

    public static ClauAgrupacio perComanda(String comanda, String empresa, Adresa adresa, InformacioEnviament informacioEnviament) {
        return new ClauAgrupacio(comanda, null, empresa, adresa, informacioEnviament);
    }

    public static ClauAgrupacio perPesa(KeyArticleClient article, String empresa, Adresa adresa, InformacioEnviament informacioEnviament) {
        return new ClauAgrupacio(null, article, empresa, adresa, informacioEnviament);
    }

    public static ClauAgrupacio perComandaPesa(String comanda, KeyArticleClient article, String empresa, Adresa adresa, InformacioEnviament informacioEnviament) {
        return new ClauAgrupacio(comanda, article, empresa, adresa, informacioEnviament);
    }

    public ModeAgrupacio tipus() {
        if (comanda != null && articleClient != null) {
            return AGRUPAR_PER_COMANDA_PESA;
        }
        if (comanda != null) {
            return AGRUPAR_PER_COMANDA;
        }
        if (articleClient != null) {
            return AGRUPAR_PER_PESA;
        }
        return AGRUPAR_TOT;
    }

}
