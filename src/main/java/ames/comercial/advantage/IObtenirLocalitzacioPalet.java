package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirLocalitzacioPalet.PaletInfo;
import ames.comercial.shared.KeyArticleClient;

import java.util.List;

public interface IObtenirLocalitzacioPalet {
    List<PaletInfo> get(KeyArticleClient articleClient);
}
