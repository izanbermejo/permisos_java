package ames.comercial.entrades.internal.application.service;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.InsertStatement;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GeneraInsertsLocalit {

    static final String ESTANTERIA_DESCARREGA = "ZZ99ZZ";
    static final String ESTANTERIA_DESCARREGA_NORMALITZATS_MEDICAL = "ZA99ZA";
    static final String CLIENT_NORMALITZAT = "000000";
    static final String MAGATZEM_MEDICAL = "0045";

    EntradaMagatzem entradaMagatzem;
    String artint;

    public GeneraInsertsLocalit (EntradaMagatzem entradaMagatzem, String artint) {
        this.entradaMagatzem = entradaMagatzem;
        this.artint = artint;
    }

    public Optional<PreparedStatementProviderBatch> genera() {
        if (entradaMagatzem.isPaletHomogeni()) {
            // En cas de ser un palet es genera un localit per cada detall que no té error
            var detallsSenseError = entradaMagatzem.detalls().stream()
                    .filter(d -> d.error().isEmpty())
                    .toList();
            return insertLocalit(entradaMagatzem, detallsSenseError);
        } else {
            // En cas que la caixa estigui marcada com a error no es genera l'INSERT al localit
            return entradaMagatzem.error().isPresent() ? Optional.empty() : insertLocalit(entradaMagatzem);
        }
    }

    private String calculaEstanteria(String client, String magatzem) {
        if (CLIENT_NORMALITZAT.equals(client) || MAGATZEM_MEDICAL.equals(magatzem))
            return ESTANTERIA_DESCARREGA_NORMALITZATS_MEDICAL;
        return ESTANTERIA_DESCARREGA;
    }

    private Optional<PreparedStatementProviderBatch> insertLocalit(EntradaMagatzem entradaMagatzem, List<EntradaMagatzemDetall> detalls) {
        if (detalls.isEmpty())
            return Optional.empty();
        return Optional.of(conn -> new MultipleInsertStatement<EntradaMagatzemDetall>(conn, "localit")
                .add("magcod", d -> entradaMagatzem.magatzem())
                .add("art", d -> entradaMagatzem.articleFabrica())
                .add("data", EntradaMagatzemDetall::dataEtiqueta)
                .add("lot", EntradaMagatzemDetall::lot)
                .add("quant", EntradaMagatzemDetall::quantitat)
                .add("estante", d -> calculaEstanteria(entradaMagatzem.client(), entradaMagatzem.magatzem()))
                .add("\"of\"", EntradaMagatzemDetall::of)
                .add("dataent", d -> entradaMagatzem.dataEntrada())
                .add("eticaja", EntradaMagatzemDetall::etiquetaCaixa)
                .add("etipale", d -> entradaMagatzem.etiquetaPalet())
                .add("fabrica", d -> entradaMagatzem.fabrica())
                .add("nivelltec", d ->  entradaMagatzem.nivellTecnic())
                .add("codseg", d -> !entradaMagatzem.codiSeguretat().isBlank() ? "S" : "")
                .add("codcal", d -> !entradaMagatzem.codiCal().isBlank() ? "S" : "")
                .add("codcli", d -> entradaMagatzem.client())
                .add("artint", d -> artint)
                .add("diareg", d -> LocalDate.now())
                .build(detalls));
    }

    private Optional<PreparedStatementProviderBatch> insertLocalit(EntradaMagatzem entradaMagatzem) {
        return Optional.of(conn -> new InsertStatement(conn, "localit")
                .add("magcod", entradaMagatzem.magatzem())
                .add("art", entradaMagatzem.articleFabrica())
                .add("data", entradaMagatzem.dataEtiqueta())
                .add("lot", entradaMagatzem.lot())
                .add("quant", entradaMagatzem.quantitat())
                .add("estante", calculaEstanteria(entradaMagatzem.client(), entradaMagatzem.magatzem()))
                .add("\"of\"", entradaMagatzem.of())
                .add("dataent", entradaMagatzem.dataEntrada())
                .add("eticaja", entradaMagatzem.etiquetaCaixa())
                .add("etipale", entradaMagatzem.etiquetaPalet())
                .add("fabrica", entradaMagatzem.fabrica())
                .add("nivelltec", entradaMagatzem.nivellTecnic())
                .add("codseg", !entradaMagatzem.codiSeguretat().isBlank() ? "S" : "")
                .add("codcal", !entradaMagatzem.codiCal().isBlank() ? "S" : "")
                .add("codcli", entradaMagatzem.client())
                .add("artint", artint)
                .add("diareg", LocalDate.now())
                .build());
    }

}
