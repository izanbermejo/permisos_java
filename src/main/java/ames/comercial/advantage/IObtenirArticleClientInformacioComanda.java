package ames.comercial.advantage;

import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.shared.KeyArticleClient;

import java.util.Optional;

public interface IObtenirArticleClientInformacioComanda {

    Optional<ArticleClientInformacioComandaResponse> executar(String articleClient);

    Optional<ArticleClientInformacioComandaResponse> executar(KeyArticleClient articleClient);

}
