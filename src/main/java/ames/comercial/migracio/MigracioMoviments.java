package ames.comercial.migracio;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.domain.service.*;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepositorySQL;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MigracioMoviments {

    @Autowired MovimentRepositorySQL movimentRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    public void migracio() {
        jdbcTemplate.execute("DELETE FROM inventari.moviment");
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from his h
						LEFT JOIN dummy d ON h.empcod = d.str1
						WHERE 1=1
						AND hisqua <> 0
						""");
        AdvantageDao.ResultSetAction<List<His>> rsAction = rs -> {
            var result = new ArrayList<His>();
            while (rs.next()) {
                result.add(new His(
                        rs.getString("artint"),
                        rs.getString("clicod"),
                        rs.getString("empcod"),
                        rs.getString("magcod"),
                        rs.getDate("hisdia").toLocalDate(),
                        TipusMoviment.fromClauAdvantage(rs.getInt("histip")),
                        rs.getLong("hisqua"),
                        rs.getString("hisfab"),
                        rs.getString("hisof"),
                        rs.getString("hisalb"),
                        rs.getString("hislalb"),
                        rs.getString("hiscom"),
                        rs.getString("hislcom"),
                        rs.getDate("diareg").toLocalDate(),
                        rs.getString("usuari"),
                        rs.getString("trascli"),
                        rs.getString("trasemp"),
                        rs.getString("trasmag"),
                        rs.getString("hismemo"),
                        rs.getLong("qtyfac")
                ));
            }
            return result;
        };
        System.out.println(LocalDateTime.now() + " Iniciant obtenció moviments HIS");
        var listHis = new AdvantageDao().query(prep, rsAction);
        System.out.println(LocalDateTime.now() + " Total registres his a migrar: " + listHis.size());
        var listMoviments = new ArrayList<Moviment>();
        for (var his : listHis) {
            try {
                var moviment = toMoviment(his);
                listMoviments.add(moviment);
            } catch (Exception e) {
                System.err.println("Error migrant moviment HIS: " + his);
                e.printStackTrace();
            }
        }
        movimentRepository.saveBatch(listMoviments);
        System.out.println(LocalDateTime.now() + " Total registres his a insertar: " + listMoviments.size());
    }

    private Moviment toMoviment(His his) {
        var articlieClient = KeyArticleClient.of(his.artint(), his.clicod());
        var empresa = his.empcod();
        var magatzem = his.magcod();
        var data = his.hisdia();
        var quantitat = his.hisqua();
        var tipus = his.tipusMoviment();
        var observacions = Optional.ofNullable(his.hismemo());
        return switch (tipus) {
            case ENTRADA -> {
                if (his.hisalb.equals("CONSUM")) {
                    var req = CrearMovimentAltresRequestImpl.builder()
                            .articleClient(articlieClient)
                            .empresa(empresa)
                            .magatzem(magatzem)
                            .data(data)
                            .tipus(TipusMoviment.REGULARITZACIO)
                            .quantitat(quantitat)
                            .observacions(observacions)
                            .build();
                    yield new CrearMovimentAltres().executar(req);
                }
                if (quantitat < 0) {
                    var reqDevFabrica = CrearMovimentDevolucioFabricaRequestImpl.builder()
                            .articleClient(articlieClient)
                            .empresa(empresa)
                            .magatzem(magatzem)
                            .data(data)
                            .quantitat(quantitat)
                            .observacions(observacions)
                            .build();
                    yield new CrearMovimentDevolucioFabrica().executar(reqDevFabrica);
                };
                var req = CrearMovimentEntradaRequestImpl.builder()
                        .articleClient(articlieClient)
                        .empresa(empresa)
                        .magatzem(magatzem)
                        .data(data)
                        .quantitat(quantitat)
                        .of(his.hisof() != null ? Long.parseLong(his.hisof()) : 0L)
                        .idEntrada(his.hisalb())
                        .idEntradaFabrica(his.hisfab())
                        .observacions(observacions)
                        .build();
                yield new FactoryMovimentEntrada().executar(req);
            }
            case SORTIDA -> {
                if (his.hisalb.equals("CONSUM")) {
                    var req = CrearMovimentAltresRequestImpl.builder()
                            .articleClient(articlieClient)
                            .empresa(empresa)
                            .magatzem(magatzem)
                            .data(data)
                            .tipus(TipusMoviment.REGULARITZACIO)
                            .quantitat(quantitat)
                            .observacions(observacions)
                            .build();
                    yield new CrearMovimentAltres().executar(req);
                }
                if (quantitat < 0) {
                    var reqDev = CrearMovimentDevolucioRequestImpl.builder()
                            .articleClient(articlieClient)
                            .empresa(empresa)
                            .magatzem(magatzem)
                            .data(data)
                            .quantitat(quantitat)
                            .parteDevolucio(his.hisalb())
                            .observacions(observacions)
                            .build();
                    yield new CrearMovimentDevolucio().executar(reqDev);
                };
                var req = CrearMovimentSortidaRequestImpl.builder()
                        .articleClient(articlieClient)
                        .empresa(empresa)
                        .magatzem(magatzem)
                        .data(data)
                        .quantitat(quantitat)
                        .client(his.clicod())
                        .liniaComanda(KeyLiniaComanda.of(
                                his.hiscom != null && !his.hiscom.isBlank() ? Long.parseLong(his.hiscom()) : 0L,
                                his.hislcom != null && !his.hislcom.isBlank() ? Long.parseLong(his.hislcom()) : 0L))
                        .liniaAlbara(readLiniaAlbara(empresa, his).orElse(KeyLiniaAlbara.of(empresa, 0L, 0L)))
                        .observacions(observacions)
                        .build();
                yield new FactoryMovimentSortida().executar(req);
            }
            case TRASPAS_CLIENT -> {
                var req = CrearMovimentTraspasClientRequestImpl.builder()
                        .articleClient(articlieClient)
                        .empresa(empresa)
                        .magatzem(magatzem)
                        .data(data)
                        .quantitat(quantitat)
                        .clientReceptor(his.trascli())
                        .isVaImplicarTraspasEmpresa(his.trasemp() != null && !his.trasemp().isBlank())
                        .observacions(observacions)
                        .build();
                yield new CrearMovimentTraspasClient().executar(req);
            }
            case TRASPAS_MAGATZEM -> {
                var req = CrearMovimentTraspasMagatzemRequestImpl.builder()
                        .articleClient(articlieClient)
                        .empresa(empresa)
                        .magatzem(magatzem)
                        .data(data)
                        .quantitat(quantitat)
                        .magatzemReceptor(his.trasmag())
                        .isVaImplicarTraspasEmpresa(his.trasemp() != null && !his.trasemp().isBlank())
                        .liniaAlbara(readLiniaAlbara(empresa, his))
                        .observacions(observacions)
                        .build();
                yield new CrearMovimentTraspasMagatzem().executar(req);
            }
            case TRASPAS_EMPRESA -> {
                var req = CrearMovimentTraspasEmpresaRequestImpl.builder()
                        .articleClient(articlieClient)
                        .empresa(empresa)
                        .magatzem(magatzem)
                        .data(data)
                        .quantitat(quantitat)
                        .empresaReceptora(his.trasemp())
                        .liniaAlbara(readLiniaAlbara(empresa, his))
                        .isVaImplicarTraspasMagatzem(his.trasmag() != null && !his.trasmag().isBlank())
                        .liniaAlbara(readLiniaAlbara(empresa, his))
                        .observacions(observacions)
                        .build();
                yield new CrearMovimentTraspasEmpresa().executar(req);
            }
            case FERRALLA, REGULARITZACIO, COMPRA_EXISTENCIES -> {
                var req = CrearMovimentAltresRequestImpl.builder()
                        .articleClient(articlieClient)
                        .empresa(empresa)
                        .magatzem(magatzem)
                        .data(data)
                        .tipus(tipus)
                        .quantitat(quantitat)
                        .observacions(observacions)
                        .build();
                yield new CrearMovimentAltres().executar(req);
            }
        };
    }

    private Optional<KeyLiniaAlbara> readLiniaAlbara(String empresa, His his) {
        return his.hisalb() != null && !his.hisalb().isBlank()
                ? Optional.of(KeyLiniaAlbara.of(
                empresa,
                his.hisalb() != null ? Long.parseLong(his.hisalb()) : 0L,
                his.hislalb() != null && !his.hislalb().isBlank() ? Long.parseLong(his.hislalb()) : 0L))
                : Optional.empty();
    }

    public record His (String artint, String clicod, String empcod, String magcod, LocalDate hisdia,
                       TipusMoviment tipusMoviment, Long hisqua, String hisfab, String hisof, String hisalb,
                       String hislalb, String hiscom, String hislcom, LocalDate datareg, String usuari,
                       String trascli, String trasemp, String trasmag, String hismemo, long qtyfac) {}

}
