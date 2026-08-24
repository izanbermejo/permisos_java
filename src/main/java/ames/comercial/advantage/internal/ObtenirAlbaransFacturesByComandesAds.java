package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByComandesResponse;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByComandesResponseImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObtenirAlbaransFacturesByComandesAds {

	public List<QueryAlbaransFacturesByComandesResponse> query(int comanda, int linia) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""					
					select c.albenv, c.empcod, c.albcod, c.albesp, f.fccnum, c.albdat, h.hisqua, c.entregat 
					from his h
					left join alblin l on h.hisalb = l.albcod and h.hislalb = l.alblin AND h.empcod = l.empcod
					left join albcap c on l.albcod = c.albcod AND l.empcod = c.empcod
					left join faclin f on f.empcod = c.empcod AND f.fclalb = c.albcod
					where hiscom = ? and hislcom = ?
					group by c.albenv, c.empcod, c.albcod, c.albesp, f.fccnum, c.albdat, h.hisqua, c.entregat
					order by c.albdat desc;
			""");
	        statement.setString(1, String.format("%07d", comanda));
	        statement.setString(2, String.format("%04d", linia));
	        return statement;
		};
		ResultSetAction<List<QueryAlbaransFacturesByComandesResponse>> rsAction = rs -> {
			List<QueryAlbaransFacturesByComandesResponse> resultat = new ArrayList<QueryAlbaransFacturesByComandesResponse>();
			while (rs.next()) {
	        	resultat.add(QueryAlbaransFacturesByComandesResponseImpl.builder()
	        		.comanda(comanda)
	        		.linia(linia)
	        		.empresa(rs.getString("empcod"))
	        		.albara(rs.getString("albcod"))
					.albaraEspecial(Optional.ofNullable(rs.getString("albesp")))
	        		.dataAlbara(rs.getDate("albdat").toLocalDate())
	        		.enviamentAlbara(rs.getString("albenv"))
	        		.factura(readSafe(rs.getString("fccnum")))
	        		.quantitat(rs.getLong("hisqua"))
	        		.entregat(readSafe(rs.getString("entregat")).toUpperCase().equals("S") ? true : false)
	        		.build());
			}
			return resultat;
		};
		return new AdvantageDao().query(prep, rsAction);
	}

	private String readSafe(String value) {
		return value != null ? value : "";
	}
			
}
