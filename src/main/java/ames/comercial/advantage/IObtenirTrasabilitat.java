package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirFifoPeses;
import ames.comercial.advantage.internal.ObtenirTrasabilitat.TrasabilitatPesa;
import ames.comercial.shared.KeyArticleClient;

import java.util.List;
import java.util.Map;

public interface IObtenirTrasabilitat {
    List<Map<String, TrasabilitatPesa>> get(KeyArticleClient articleClient);

}
