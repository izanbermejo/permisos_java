package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.services.ProviderInformacioArticleclient.InformacioArticleclient;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;

import java.util.Collection;

public interface IProviderInformacioArticleclient {

    IProviderInformacioArticleclientResponse provide(Collection<KeyArticleClient> articleClients);

    interface IProviderInformacioArticleclientResponse {
        InformacioArticleclient get(KeyArticleClient articleClient);
        Empresa empresa();
        Empresa empresaDesti();
    }

}
