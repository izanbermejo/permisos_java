package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirInformacioAcumulatsArticleClientAds.InformacioAcumulatsArticleClient;
import ames.comercial.shared.KeyArticleClient;

public interface IObtenirInformacioAcumulatsArticleClient {
    InformacioAcumulatsArticleClient get(KeyArticleClient articleClient);
}
