package ames.comercial.comandes.internal.infraestructure.liniacomanda;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.server.ETag;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LiniaComandaRepository {

	long nextNumero(long comanda);
	
	void save (LiniaComanda l);
	void save (List<LiniaComanda> l);
	
	Optional<LiniaComanda> find (long comanda, long numero);
	Optional<LiniaComanda> find (KeyLiniaComanda clauLiniaComanda);
	List<LiniaComanda> find (List<KeyLiniaComanda> ids);
	Optional<LiniaComanda> findStockSeguretat (KeyArticleClient articleClient);
	List<LiniaComanda> findByComanda (long comanda);
	List<LiniaComanda> findByComanda (long comanda, boolean includeZero);
	List<LiniaComanda> findByArticlePendent (KeyArticleClient articleClient);
	List<LiniaComanda> findByArticlePendent (KeyArticleClient articleClient, Empresa empresa);
	List<LiniaComanda> findByArticlePendent (KeyArticleClient articleClient, Empresa empresa, boolean includeZero);

	List<Pair<LocalDateTime, LiniaComanda>> findByMomentPosterior(LocalDateTime moment);
	Optional<Pair<LocalDateTime, LiniaComanda>> findVersioAnterior(KeyLiniaComanda clauLinia, LocalDateTime moment);

	long quantitatPendent (long comanda);

	Optional<LocalDate> dataUltimaComanda(KeyArticleClient articleClient);

	void checkEtag(long comanda, long numero);
	Optional<ETag> etag(long comanda, long numero);

}
