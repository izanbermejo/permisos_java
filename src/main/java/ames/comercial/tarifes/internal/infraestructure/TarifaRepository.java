package ames.comercial.tarifes.internal.infraestructure;

import ames.comercial.tarifes.beans.Preu;
import ames.comercial.tarifes.internal.domain.DadesTarifa;
import ames.comercial.tarifes.internal.infraestructure.mapper.PreuRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TarifaRepository {

	public long nextId();

	public long nextPreuId();

	public Long save (DadesTarifa dadesTarifa);

	public void savePreus (Long codi_tarifa, List<Preu> preus);

	public Optional<DadesTarifa> find (Long codi);

	public Optional<DadesTarifa> findByNom (String nom);

	public Optional<DadesTarifa> findByCodi (Long codi);

	public boolean exists(Long codi);

	public boolean exists(String nom);

	public List<Preu> findPreus(Long codi_tarifa);

	public List<PreuRecord> findPreusRecord(Long codi_tarifa);

	public List<DadesTarifa> findTarifes(String name,String divisa,Integer any,String estat);

//	public int updateTarifaField(String tarifa, String camp, Object value);

	public int updateTarifaFields(Long codi, HashMap<String, Object> fields);

	public int delete(Long codi);

	public int deletePreus(Long codi_tarifa);

//	void checkEtag(long codi);

//	Optional<ETag> etag(long codi);
	
}
