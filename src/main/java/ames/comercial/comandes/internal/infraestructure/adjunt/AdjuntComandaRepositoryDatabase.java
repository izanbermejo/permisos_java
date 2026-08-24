package ames.comercial.comandes.internal.infraestructure.adjunt;

import ames.comercial.comandes.internal.domain.Adjunt;

import java.util.List;
import java.util.Optional;

public interface AdjuntComandaRepositoryDatabase {
	
	void add (String codiAdjunt, Adjunt a);
	void remove (long comanda, String codiAdjunt);
	Optional<Adjunt> find (long comanda, String codiAdjunt);
	Optional<Adjunt> findByNom (long comanda, String nom);
	List<String> findNomSimilar (long comanda, String nom);
	List<Adjunt> findByComanda(long comanda);

}
