package ames.comercial.comandes.internal.infraestructure.comanda;

import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.server.ETag;
import ames.comercial.shared.Empresa;

import java.util.List;
import java.util.Optional;

public interface ComandaRepository {

	void save (Comanda c);

	Optional<Comanda> find (long id);
	List<Comanda> find (List<Long> ids);
	Optional<Comanda> findByComandaClient(String client, String comandaClient, Empresa empresa);

	void updateNumAdjunts(long comanda);

	void checkEtag(long codi);
	Optional<ETag> etag(long codi);
	
}
