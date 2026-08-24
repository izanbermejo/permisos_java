package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.response.AlbaraServitLiniaComanda;
import ames.comercial.shared.KeyArticleClient;

import java.util.List;
import java.util.Map;

/**
 * Exposa, fora del mòdul de comandes, els albarans que han servit les línies de comanda d'un article-client,
 * agrupats per línia. Ho fa servir la proposta de traspàs per mostrar l'historial de servits de cada línia.
 */
public interface IObtenirAlbaransServitsPerLinia {

    Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>> executar(KeyArticleClient articleClient);

}
