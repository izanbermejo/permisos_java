package ames.comercial.edi.internal.infraestructure.comandaEDI;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;

import java.util.HashMap;
import java.util.List;

public interface ComandaEDIRepository {

	public long nextId();

	public Long save (DadesComandaEDI dadesComandaEDI);

	public void saveLinies (List<DadesLiniaEDI> linies);

	public int updateComandaEDIFields(Long codi, HashMap<String, Object> fields);

	public int updateLiniaComandaEDIFields(Long codi, HashMap<String, Object> fields);

	public int setPathPDFofPathEDIFiles(String pathPDF, String pathEDI);

	public  void delete (long codi);

	public void updateUltimAlbara(Long codiComanda,String codiArticle, String ultimAlbara);

	public void deleteLiniesArticle(Long codiComanda,String codiArticle);

	public void updateComandaSetProcessada(Long codiComanda);

	public void updateLiniaSetProcessada(Long codiComanda, String codiArticle);

	public void updateClientProfile(Long codiComanda, ObtenirClientEDIAds.ClientEDIAds clientProfile);

	void fixComandaWithNADIssue(Long codiComanda, String codiClientAmes, String nomClientAmes);

	void fixLiniaWithNADIssue(Long codiComanda, String codiClientAmes, String nomClientAmes);

//	void fixLineWithNADIssue(DadesComandaEDI dadesComandaEDI,String codiClientAmes, String nomClientAmes);

//	void checkEtag(long codi);

//	Optional<ETag> etag(long codi);
	
}
