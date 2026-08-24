package ames.comercial.comandes.internal.infraestructure.query;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.response.AlbaraServitLiniaComanda;
import ames.comercial.comandes.response.ItemHistoriaLiniaComanda;
import ames.comercial.comandes.response.ItemLiniaComanda;
import ames.comercial.shared.KeyArticleClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface QueryRepository {

	List<ItemLiniaComanda> searchLiniesPeriode(String artint, String clicod, LocalDate dataInicial, LocalDate dataFinal);

	List<ItemHistoriaLiniaComanda> searchHistoriaLiniaComanda(long comanda, long numero);

	/**
	 * Retorna els albarans que han servit cada línia de comanda de l'article+client indicat,
	 * agrupats per línia (clau {@code comanda + "_" + numero}). Obté la informació del nou sistema
	 * ({@code inventari.moviment} + {@code albarans.albara} + {@code albarans.facturacio}) en una
	 * sola consulta, substituint la crida per línia a Advantage.
	 */
	Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>> searchAlbaransServitsPerLinia(KeyArticleClient articleClient);

}
