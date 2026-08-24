package ames.comercial.migracio;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.*;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepositorySQL;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MigracioLiniesAlbara {

    @Autowired LiniaAlbaraRepositorySQL liniaAlbaraRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    public void migracio() {
        jdbcTemplate.execute("""
                DELETE FROM albarans.linia_albara AS la
                USING albarans.albara AS a
                WHERE a.empresa=la.empresa AND a.codi=la.codi_albara
                  AND a.data >= '2025-01-01';
                """);

        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                SELECT l.*
                FROM alblin l
                LEFT JOIN albcap a ON l.albcod = a.albcod AND l.empcod = a.empcod
                WHERE 1=1 AND year(albdat) >= 2025
                ORDER BY a.DIAREG ASC, a.empcod ASC, a.albcod ASC;
                """);
        AdvantageDao.ResultSetAction<List<Alblin>> rsAction = rs -> {
            var result = new ArrayList<Alblin>();
            while (rs.next()) {
                result.add(new Alblin(
                        rs.getLong("albcod"),
                        rs.getString("empcod"),
                        rs.getLong("alblin"),
                        rs.getString("artint"),
                        rs.getString("clicod"),
                        !rs.getString("comcod").isBlank() ? rs.getLong("comcod") : null,
                        rs.getString("acomsc"),
                        rs.getString("aprosc"),
                        rs.getLong("albqua"),
                        rs.getBigDecimal("albpre"),
                        rs.getString("albdiv"),
                        rs.getString("albopc"),
                        rs.getString("observ"),
                        rs.getString("notes"),
                        rs.getLong("penfac"),
                        rs.getLong("qtypendent"),
                        rs.getString("codiemba"),
                        rs.getDate("diareg").toLocalDate(),
                        rs.getString("usuari"),
                        rs.getString("pecaexpres")
                ));
            }
            return result;
        };
        System.out.println(LocalDateTime.now() + " Iniciant obtenció línies ALBLIN");
        var listAlblin = new AdvantageDao().query(prep, rsAction);
        System.out.println(LocalDateTime.now() + " Total registres alblin a migrar: " + listAlblin.size());
        var listLinies = new ArrayList<LiniaAlbara>();
        for (var alblin : listAlblin) {
            try {
                var linia = toLiniaAlbara(alblin);
                listLinies.add(linia);
            } catch (Exception e) {
                System.err.println("Error migrant Línia Albarà: " + alblin);
                e.printStackTrace();
            }
        }
        liniaAlbaraRepository.saveBatch(listLinies);
    }

    private LiniaAlbara toLiniaAlbara(Alblin alblin) {
        var keyAlbara = KeyAlbara.of(alblin.albcod(), alblin.empcod());
        var keyLinia = KeyLiniaAlbara.of(keyAlbara, alblin.alblin());

        // Processar informació comanda (si existeix)
        Optional<InformacioComanda> infoComanda = Optional.empty();
        infoComanda = Optional.of(InformacioComandaImpl.builder()
                .comanda(Optional.ofNullable(alblin.comcod()))
                .comandaClient(nvl(alblin.acomsc(), ""))
                .programa(nvl(alblin.aprosc(), ""))
                .build());

        // InfoPesa buida (aquesta informació es migra a posteriori
        // amb una query amb la informació de l'articleclient)
        var infoPesa = InformacioPesa.empty();

        // Processar preu amb divisa
        Divisa divisa;
        try {
            divisa = Divisa.getBySymbol(nvl(alblin.albdiv(), "EUR"));
        } catch (Exception e) {
            divisa = Divisa.EURO;
        }

        var preu = Preu.of(nvl(alblin.albpre(), BigDecimal.ZERO), divisa);

        // Construir LiniaAlbara
        var liniaBuilder = LiniaAlbaraImpl.builder()
                .id(keyLinia)
                .articleClient(KeyArticleClient.of(
                        nvl(alblin.artint(), ""),
                        nvl(alblin.clicod(), "")
                ))
                .infoComanda(infoComanda)
                .informacioPesa(infoPesa)
                .quantitat(nvl(alblin.albqua(), 0L))
                .preu(preu)
                .quantitatPendentFacturar(nvl(alblin.penfac(), 0L))
                .quantitatPendentConsumir(nvl(alblin.qtypendent(), 0L))
                .isPreuFixat(alblin.albopc() != null && alblin.albopc().equalsIgnoreCase("2"))  // Si albopc = 2, preu fixat a la comanda manualment
                .comandaBlanca(alblin.albopc() != null && alblin.albopc().equalsIgnoreCase("8") ? Optional.of(8L) : Optional.empty())
                .dataregMigracio(alblin.datareg())
                .usuregMigracio(alblin.usuari())
                .isUrgent(isTrue(alblin.pecaexpres()));

        // Camps opcionals
        if (alblin.observ() != null && !alblin.observ().isBlank()) {
            liniaBuilder.observacionsImpressio(alblin.observ());
        }
        if (alblin.notes() != null && !alblin.notes().isBlank()) {
            liniaBuilder.observacionsInternes(alblin.notes());
        }
        if (alblin.codiemba() != null && !alblin.codiemba().isBlank()) {
            liniaBuilder.codiEmbalatge(alblin.codiemba());
        }

        return liniaBuilder.build();
    }

    private <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    private boolean isTrue(String value) {
        return "S".equalsIgnoreCase(value) || "T".equalsIgnoreCase(value) || "1".equals(value);
    }

    public record Alblin(
            Long albcod,
            String empcod,
            Long alblin,
            String artint,
            String clicod,
            Long comcod,
            String acomsc,
            String aprosc,
            Long albqua,
            BigDecimal albpre,
            String albdiv,
            String albopc,
            String observ,
            String notes,
            Long penfac,
            Long qtypendent,
            String codiemba,
            LocalDate datareg,
            String usuari,
            String pecaexpres
    ) {}

}

