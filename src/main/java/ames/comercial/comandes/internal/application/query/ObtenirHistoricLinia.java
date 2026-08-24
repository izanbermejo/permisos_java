package ames.comercial.comandes.internal.application.query;

import java.util.List;

import org.springframework.stereotype.Service;

import ames.comercial.comandes.internal.infraestructure.query.QueryRepository;
import ames.comercial.comandes.response.ItemHistoriaLiniaComanda;

@Service
public class ObtenirHistoricLinia {
	
	QueryRepository queryRepo;
	
	public ObtenirHistoricLinia(QueryRepository queryRepo) {
		this.queryRepo = queryRepo;
	}

	public List<ItemHistoriaLiniaComanda> executar (long comanda, long numero) {
		return queryRepo.searchHistoriaLiniaComanda(comanda, numero);
	}
	
}
