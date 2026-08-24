package ames.comercial.inventari.internal.infraestructure.moviment.mapper;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.inventari.internal.domain.moviment.*;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MovimentMapper implements RowMapper<Moviment> {

    @Override
    public Moviment mapRow(ResultSet rs, int rowNum) throws SQLException {
        var builder = MovimentImpl.builder()
                .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                .empresa(rs.getString("empresa"))
                .magatzem(rs.getString("magatzem"))
                .data(rs.getDate("data").toLocalDate())
                .tipus(TipusMoviment.valueOf(rs.getString("tipus")))
                .quantitat(rs.getLong("quantitat"))
                .liniaAlbara(mapLiniaAlbara(rs))
                .observacions(Optional.ofNullable(rs.getString("observacions")))
                .dataCreacio(rs.getTimestamp("data_creacio").toLocalDateTime())
                .usuari(rs.getString("usuari"));

        // InformacioEntrada
        mapEntrada(rs).ifPresent(builder::entrada);

        // InformacioSortida
        mapSortida(rs).ifPresent(builder::sortida);

        // InformacioDevolucio
        mapDevolucio(rs).ifPresent(builder::devolucio);

        // InformacioTraspasClient
        mapTraspasClient(rs).ifPresent(builder::traspasClient);

        // InformacioTraspasMagatzem
        mapTraspasMagatzem(rs).ifPresent(builder::traspasMagatzem);

        // InformacioTraspasEmpresa
        mapTraspasEmpresa(rs).ifPresent(builder::traspasEmpresa);

        return builder.build();
    }

    private Optional<KeyLiniaAlbara> mapLiniaAlbara(ResultSet rs) throws SQLException {
        String albaraEmpresa = rs.getString("linia_albara_empresa");
        if (rs.wasNull()) {
            return Optional.empty();
        }
        return Optional.of(KeyLiniaAlbara.of(
                KeyAlbara.of(
                        rs.getLong("linia_albara_numero"),
                        albaraEmpresa
                ),
                rs.getLong("linia_albara_linia")
        ));
    }

    private Optional<InformacioEntrada> mapEntrada(ResultSet rs) throws SQLException {
        Long of = rs.getObject("entrada_of", Long.class);
        if (of == null) {
            return Optional.empty();
        }

        return Optional.of(InformacioEntradaImpl.builder()
                .of(of)
                .idEntrada(rs.getString("entrada_id_entrada"))
                .idEntradaFabrica(rs.getString("entrada_id_entrada_fabrica"))
                .build());
    }

    private Optional<InformacioSortida> mapSortida(ResultSet rs) throws SQLException {
        String client = rs.getString("sortida_client");
        if (client == null) {
            return Optional.empty();
        }

        // Tota sortida prové d'una línia de comanda (també els consums, que serveixen la comanda pendent FIFO)
        var informacioSortida = InformacioSortidaImpl.builder()
                .client(client)
                .liniaComanda(KeyLiniaComanda.of(rs.getLong("sortida_comanda_codi"), rs.getLong("sortida_comanda_numero")))
                .build();

        return Optional.of(informacioSortida);
    }

    private Optional<InformacioDevolucio> mapDevolucio(ResultSet rs) throws SQLException {
        String parteDevolucio = rs.getString("devolucio_parte_devolucio");
        if (parteDevolucio == null) {
            return Optional.empty();
        }

        return Optional.of(InformacioDevolucioImpl.builder()
                .parteDevolucio(parteDevolucio)
                .build());
    }

    private Optional<InformacioTraspasClient> mapTraspasClient(ResultSet rs) throws SQLException {
        String clientReceptor = rs.getString("traspas_client_receptor");
        if (clientReceptor == null) {
            return Optional.empty();
        }

        return Optional.of(InformacioTraspasClientImpl.builder()
                .clientReceptor(clientReceptor)
                .isVaImplicarTraspasEmpresa(rs.getBoolean("traspas_client_traspas_empresa"))
                .build());
    }

    private Optional<InformacioTraspasMagatzem> mapTraspasMagatzem(ResultSet rs) throws SQLException {
        String magatzemReceptor = rs.getString("traspas_magatzem_receptor");
        if (magatzemReceptor == null) {
            return Optional.empty();
        }

        return Optional.of(InformacioTraspasMagatzemImpl.builder()
                .magatzemReceptor(magatzemReceptor)
                .isVaImplicarTraspasEmpresa(rs.getBoolean("traspas_magatzem_traspas_empresa"))
                .build());
    }

    private Optional<InformacioTraspasEmpresa> mapTraspasEmpresa(ResultSet rs) throws SQLException {
        String empresaReceptora = rs.getString("traspas_empresa_receptora");
        if (empresaReceptora == null) {
            return Optional.empty();
        }

        var builder = InformacioTraspasEmpresaImpl.builder()
                .empresaReceptora(empresaReceptora)
                .isVaImplicarTraspasMagatzem(rs.getBoolean("traspas_empresa_traspas_magatzem"));

        return Optional.of(builder.build());
    }

}

