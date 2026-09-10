package ames.permisos.organigrama.internal.application.query;

import ames.permisos.aplicacions.AplicacionsException;
import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepository;
import ames.permisos.organigrama.OrganigramaException;
import ames.permisos.organigrama.internal.infraestructure.EmpleatRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ObtenirPermisosEmpleat {

    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired EmpleatRepository empleatRepository;
    @Autowired AplicacioRepository aplicacioRepository;

    public ObtenirPermisosEmpleatResponse executar(long usufab, String aplicacio) {
        var empleat = empleatRepository.findByUsufab(usufab).orElseThrow(OrganigramaException.EmpleatNoTrobatPermisos::new);
        aplicacioRepository.find(aplicacio).orElseThrow(AplicacionsException.AplicacioNoTrobadaPermisos::new);
        return ObtenirPermisosEmpleatResponseImpl.builder()
                .moduls(obtenirPermisosModuls(empleat.id(), aplicacio))
                .parametres(obtenirParametresAplicacio(empleat.id(), aplicacio))
                .build();
    }

    private Map<String, List<String>> obtenirPermisosModuls(long idEmpleat, String aplicacio) {
        String sql = """
        -- FUNCIONS ASSIGNADES A L'EMPLEAT
        WITH funcions AS (
            SELECT fdc.centre, fdc.departament, fdc.funcio
            FROM organigrama.empleat_funcio ef
            LEFT JOIN organigrama.funcions_departament_centre fdc
                   ON ef.id_func_dep_cent = fdc.id
            WHERE ef.id_persona = :idEmpleat
        ),

        -- MODULS ASSIGNATS PER FUNCIO (AMB CROSSJOIN PER COMBINAR TOTES LES FUNCIONS AMB ELS
        -- MODULS I AIXÍ PODER TENIR EN COMPTE ELS COMODIS QUE SÓN ELS NULLS DE CADA COLUMNA)
        moduls_funcio AS (
            SELECT DISTINCT fm.nom_modul
            FROM organigrama_permisos.funcio_modul fm
            CROSS JOIN funcions f
            WHERE fm.nom_aplicacio = :aplicacio
              AND (fm.id_centre IS NULL OR fm.id_centre = f.centre)
              AND (fm.id_departament IS NULL OR fm.id_departament = f.departament)
              AND (fm.id_funcio IS NULL OR fm.id_funcio = f.funcio)
        ),

        -- MODULS ASSIGNATS DIRECTAMENT A L'EMPLEAT
        moduls_empleat AS (
            SELECT DISTINCT nom_modul
            FROM organigrama_permisos.empleat_modul
            WHERE nom_aplicacio = :aplicacio
              AND id_empleat = :idEmpleat
        ),

        -- UNIÓ DE MODULS
        moduls_totals AS (
            SELECT nom_modul FROM moduls_funcio
            UNION
            SELECT nom_modul FROM moduls_empleat
        ),

        -- PERMISOS ASSIGNATS PER FUNCIO (AMB CROSSJOIN PER COMBINAR TOTES LES FUNCIONS AMB ELS
        -- PERMISOS I AIXÍ PODER TENIR EN COMPTE ELS COMODIS QUE SÓN ELS NULLS DE CADA COLUMNA)
        permisos_funcio AS (
            SELECT fp.nom_modul, fp.nom_permis
            FROM organigrama_permisos.funcio_permis fp
            CROSS JOIN funcions f
            WHERE fp.nom_aplicacio = :aplicacio
              AND fp.nom_modul IN (SELECT nom_modul FROM moduls_totals)
              AND (fp.id_centre IS NULL OR fp.id_centre = f.centre)
              AND (fp.id_departament IS NULL OR fp.id_departament = f.departament)
              AND (fp.id_funcio IS NULL OR fp.id_funcio = f.funcio)
        ),

        -- PERMISOS ASIGNATS DIRECTAMENT A L'EMPLEAT
        permisos_empleat AS (
            SELECT nom_modul, nom_permis
            FROM organigrama_permisos.empleat_permis
            WHERE nom_aplicacio = :aplicacio
              AND id_empleat = :idEmpleat
              AND nom_modul IN (SELECT nom_modul FROM moduls_totals)
        ),

        -- UNIÓ DE PERMISOS
        permisos_totals AS (
            SELECT * FROM permisos_funcio
            UNION
            SELECT * FROM permisos_empleat
        )

        -- JOIN FINAL ENTRE MODULS I PERMISOS, ES FA LEFT JOIN PERQUÈ POT HAVER MODULS SENSE PERMISOS
        SELECT 
            mt.nom_modul,
            pt.nom_permis
        FROM moduls_totals mt
        LEFT JOIN permisos_totals pt ON mt.nom_modul = pt.nom_modul
        ORDER BY mt.nom_modul, pt.nom_permis
    """;
        Map<String, Object> params = Map.of(
                "idEmpleat", idEmpleat,
                "aplicacio", aplicacio
        );
        return jdbc.query(sql, params, rs -> {
            Map<String, List<String>> mapa = new HashMap<>();
            while (rs.next()) {
                String modul = rs.getString("nom_modul");
                String permis = rs.getString("nom_permis");
                mapa.computeIfAbsent(modul, k -> new ArrayList<>()).add(permis);
            }
            return mapa;
        });
    }

    private Map<String, List<String>> obtenirParametresAplicacio(long idEmpleat, String aplicacio) {
        String sql = """
            WITH funcions AS (
                SELECT fdc.centre, fdc.departament, fdc.funcio
                FROM organigrama.empleat_funcio ef
                LEFT JOIN organigrama.funcions_departament_centre fdc ON ef.id_func_dep_cent = fdc.id
                WHERE ef.id_persona = :idEmpleat
            ),
            parametres_per_funcio AS (
                SELECT fd.nom_parametre, fd.valor
                FROM organigrama_permisos.funcio_parametre fd
                CROSS JOIN funcions f
                WHERE fd.nom_aplicacio = :aplicacio
                  AND (fd.id_centre IS NULL OR fd.id_centre = f.centre)
                  AND (fd.id_departament IS NULL OR fd.id_departament = f.departament)
                  AND (fd.id_funcio IS NULL OR fd.id_funcio = f.funcio)
            ),
            parametres_per_empleat AS (
                SELECT nom_parametre, valor
                FROM organigrama_permisos.empleat_parametre
                WHERE nom_aplicacio = :aplicacio
                  AND id_empleat = :idEmpleat
            ),
            parametres_totals AS (
                SELECT * FROM parametres_per_funcio
                UNION
                SELECT * FROM parametres_per_empleat
            )
            SELECT nom_parametre, valor
            FROM parametres_totals
            ORDER BY nom_parametre, valor
        """;
        Map<String, Object> params = Map.of(
                "idEmpleat", idEmpleat,
                "aplicacio", aplicacio
        );
        return jdbc.query(sql, params, rs -> {
            Map<String, List<String>> mapa = new HashMap<>();
            while (rs.next()) {
                String parametre = rs.getString("nom_parametre");
                String valor = rs.getString("valor");
                mapa.computeIfAbsent(parametre, k -> new ArrayList<>()).add(valor);
            }
            return mapa;
        });
    }

    @JsonDeserialize(builder = ObtenirPermisosEmpleatResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirPermisosEmpleatResponse {
        Map<String, List<String>> moduls();
        Map<String, List<String>> parametres();
    }

}
