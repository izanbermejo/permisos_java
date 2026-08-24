package ames.comercial.clients.internal.infraestructure.adjunt;

import ames.comercial.clients.internal.domain.Adjunt;

import java.util.List;
import java.util.Optional;

public interface AdjuntClientRepositoryDatabase {
	
	void add (String codiAdjunt, Adjunt a);
	void remove (String codiClient, String codiAdjunt);
	Optional<Adjunt> find (String codiClient, String codiAdjunt);
	List<String> findNomSimilar (String codiClient, String nom);

}
