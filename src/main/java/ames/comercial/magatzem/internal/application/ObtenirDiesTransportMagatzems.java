package ames.comercial.magatzem.internal.application;

import ames.comercial.magatzem.ext.IObtenirDiesTransportMagatzems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ObtenirDiesTransportMagatzems implements IObtenirDiesTransportMagatzems {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public long get(String magatzemEntrada, String magatzemSortida) {
        try {
            Long dies = jdbcAmes.queryForObject("""
                    SELECT dies_transport FROM com_magatzem.magatzems_intermitjos
                    WHERE inicial = ? AND final = ?
                    """, Long.class, magatzemEntrada, magatzemSortida);
            return dies == null ? 0L : dies;
        } catch (EmptyResultDataAccessException e) {
            return 0L;
        }
    }

}
