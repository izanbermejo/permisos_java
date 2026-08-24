package ames.comercial.migracio;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.inventari.internal.domain.fitxa.Fitxa;
import ames.comercial.inventari.internal.domain.fitxa.FitxaImpl;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxaImpl;
import ames.comercial.inventari.internal.infraestructure.fitxa.FitxaRepositorySQL;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MigracioFitxes {

    @Autowired FitxaRepositorySQL fitxaRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    public void migracio() {
        jdbcTemplate.execute("DELETE FROM inventari.fitxa");

        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from artfit a
						LEFT JOIN dummy d ON a.empcod = d.str1
						WHERE a.empcod <> '' and a.magcod <> '' and a.clicod <> '' and a.artint <> ''
						""");
        AdvantageDao.ResultSetAction<List<Artfit>> rsAction = rs -> {
            var result = new ArrayList<Artfit>();
            while (rs.next()) {
                result.add(new Artfit(
                        rs.getString("artint"),
                        rs.getString("clicod"),
                        rs.getString("empcod"),
                        rs.getString("magcod"),
                        rs.getLong("fitstkrac"),
                        rs.getString("fitany"),
                        rs.getLong("comqres"),
                        rs.getString("actiu")
                ));
            }
            return result;
        };
        System.out.println(LocalDateTime.now() + " Iniciant obtenció fitxes ARTFIT");
        var listArtfits = new AdvantageDao().query(prep, rsAction);
        System.out.println(LocalDateTime.now() + " Total registres his a migrar: " + listArtfits.size());
        var listFitxes = new ArrayList<Fitxa>();
        for (var artfit : listArtfits) {
            try {
                var fitxa = toFitxa(artfit);
                listFitxes.add(fitxa);
            } catch (Exception e) {
                System.err.println("Error migrant Fitxa: " + artfit);
                e.printStackTrace();
            }
        }
        fitxaRepository.saveBatch(listFitxes);
        System.out.println(LocalDateTime.now() + " Total registres fitxes a insertar: " + listFitxes.size());
    }

    private Fitxa toFitxa(Artfit artfit) {
        return FitxaImpl.builder()
                .id(KeyFitxaImpl.builder()
                        .articleClient(KeyArticleClient.of(artfit.artint, artfit.clicod))
                        .empresa(artfit.empcod)
                        .magatzem(artfit.magcod)
                        .build())
                .stock(artfit.fitstkrac)
                .stockReservat(artfit.comqres())
                .isActiu(artfit.actiu().equalsIgnoreCase("S"))
                .dataCreacio(LocalDate.of(Integer.parseInt(artfit.fitany), 1, 1))
                .build();
    }


    public record Artfit (String artint, String clicod, String empcod, String magcod, long fitstkrac,
                       String fitany, long comqres, String actiu) {}

}
