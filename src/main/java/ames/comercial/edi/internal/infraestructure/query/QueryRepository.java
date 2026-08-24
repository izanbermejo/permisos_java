package ames.comercial.edi.internal.infraestructure.query;

import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.shared.KeyArticleAmes;
import ames.comercial.shared.Usuari;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface QueryRepository {

	public Optional<DadesComandaEDI> find (long id);

	public Optional<DadesComandaEDINoJSON> findNoJSON (long id);

	public List<DadesComandaEDINoJSON> list ();

	public List<DadesComandaEDINoJSON> list (String usuari);

	public boolean existComandaWithPathEDI(String pathEDI);

	public boolean existLiniaComandaEDI(Long codi);

	public List<KeyArticleAmes> listArticles(Long codi_comanda);

	public List<KeyArticleAmes> listArticles (Long codi_comanda,String diesPrevisio);

	public List<Usuari> listUsuaris ();

//	boolean comandaProcessada(Long codiComanda);

//	public List<DadesComandaEDINoJSON> listComandesSensePathPDF();

	public List<String> listComandesSensePathPDF();

	public List<DadesLiniaEDI> listLinies(Long codi_comanda);

	public List<DadesLiniaEDI> listLinies(Long codi_comanda, String diesPrevisio);

	public List<DadesLiniaEDI> listLinies(Long codi_comanda, String diesPrevisio, String codi_article);

	public Optional<KeyArticleAmes> getFirstArticleOfOrderLines(Long codi_comanda);

	public boolean ultimArticleDeComanda(Long codiComanda);

//	void checkEtag(long codi);

//	Optional<ETag> etag(long codi);
	
}
