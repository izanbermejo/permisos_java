package ames.comercial.migracio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Completa el pendent de consumir de les línies dels albarans de traspàs a plataforma perquè quadri amb
 * l'stock dels magatzems plataforma.
 * <p>
 * A l'Advantage el pendent de consumir només es mantenia en els magatzems que estaven marcats per
 * fer-ho; ara la funcionalitat de consums s'aplica a tots els magatzems de tipus {@code PLATAFORMA}, de
 * manera que els traspassos dels magatzems que no es controlaven arriben amb el pendent de consumir a
 * zero tot i tenir stock a la plataforma. Mentre no quadri, els consums d'aquestes peces fallen amb
 * {@code PendentConsumirInsuficient}.
 * <p>
 * L'invariant que ha de complir cada peça d'una plataforma és:
 * <b>stock del magatzem plataforma = suma del pendent de consumir dels traspassos que hi entren</b>
 * (l'entrada del traspàs incrementa l'stock i el consum el descompta alhora que descompta el pendent).
 * El magatzem plataforma d'un traspàs és el receptor
 * ({@code informacio_traspas ->> 'magatzemReceptor'}), no el magatzem de l'albarà, que és l'origen.
 * <p>
 * <b>Es respecta el que s'ha migrat</b>: les línies que ja tenen pendent de consumir &gt; 0 venen d'un
 * magatzem que sí que es controlava a l'Advantage i es donen per bones, no es toquen mai. Només s'omplen
 * les línies que tenen el pendent a zero, amb l'stock que queda per assignar
 * ({@code stock - pendent de les línies que ja en tenen}), en <b>ordre FIFO invers</b> (de la més nova a
 * la més antiga) i limitat per la quantitat de cada línia. És l'invers del criteri de consum
 * ({@code ConsumirPendentTraspasFIFO} consumeix primer els traspassos més antics), de manera que el
 * pendent viu queda concentrat en els traspassos més nous, com si la funcionalitat hagués estat activa
 * des del principi. Com que recalcula (no ajusta diferències), és idempotent.
 * <p>
 * No s'inventa pendent ni se'n treu: els grups que no es poden quadrar només s'informen, a
 * {@link ResultatMigracio#descompensats()}, amb el motiu al detall
 * ({@code stockNoAssignat} o {@code excesPendent} &gt; 0). Tampoc no es replica res a l'Advantage: és una
 * correcció de dades del PostgreSQL sobre magatzems que l'Advantage no controlava.
 * <p>
 * S'executa amb {@code isSimulacio = true} per obtenir només l'informe (l'stock de plataformes que no
 * quadra amb els traspassos, grup a grup i línia a línia) i amb {@code isSimulacio = false} per
 * aplicar-ho. Ha d'anar després de {@code MigracioFitxes}, {@code MigracioLiniesAlbara} i
 * {@code MigracioMagatzems} (els magatzems marcats com a plataforma sense ser-ho han de quedar com a
 * {@code TRANSIT}, si no compten a totes dues bandes).
 * TODO: Eliminar després de la migració.
 */
@Component
public class MigracioPendentConsumirPlataforma {

    private static final int MIDA_LOT = 2000;

    @Autowired JdbcTemplate jdbcAmes;

    public ResultatMigracio migracio(boolean isSimulacio) {
        System.out.println(LocalDateTime.now() + " Iniciant recàlcul del pendent de consumir de plataformes (simulacio=" + isSimulacio + ")");
        var liniesPerGrup = obtenirLiniesTraspasPlataforma();
        var stocks = obtenirStocksPlataforma();
        System.out.println(LocalDateTime.now() + " Grups amb traspassos: " + liniesPerGrup.size() + " / grups amb stock: " + stocks.size());

        var canvis = new ArrayList<CanviLinia>();
        var descompensats = new ArrayList<GrupDescompensat>();

        // Grups amb línies de traspàs: s'omplen les línies que tenen el pendent a zero amb l'stock que
        // queda per assignar, en ordre FIFO invers (les línies amb pendent > 0 no es toquen)
        for (var grup : liniesPerGrup.entrySet()) {
            var clau = grup.getKey();
            var linies = grup.getValue();
            // Un stock negatiu no es pot repartir; es tracta com a zero i el grup queda informat
            long stock = Math.max(stocks.getOrDefault(clau, 0L), 0L);

            // Pendent de les línies que ja en tenen: es dona per bo (ve de l'Advantage) i és intocable
            long pendentFixat = linies.stream()
                    .mapToLong(LiniaTraspas::pendentConsumir)
                    .filter(pendent -> pendent > 0)
                    .sum();

            // Stock que queda per assignar a les línies que estan a zero. Si el pendent que es dona per
            // bo ja supera l'stock, no s'assigna res (no es redueix cap línia migrada) i s'informa.
            long restant = Math.max(stock - pendentFixat, 0);
            long excesPendent = Math.max(pendentFixat - stock, 0);
            long pendentAssignat = 0;

            for (var linia : linies) {
                if (restant <= 0) {
                    break;
                }
                if (linia.pendentConsumir() > 0) {
                    continue;
                }
                // El pendent d'una línia no pot superar mai la seva quantitat
                long pendentDespres = Math.min(restant, linia.quantitat());
                if (pendentDespres > 0) {
                    canvis.add(new CanviLinia(clau, linia.codiAlbara(), linia.linia(),
                            linia.quantitat(), linia.pendentConsumir(), pendentDespres));
                    restant -= pendentDespres;
                    pendentAssignat += pendentDespres;
                }
            }

            if (pendentAssignat > 0 || restant > 0 || excesPendent > 0) {
                descompensats.add(new GrupDescompensat(clau, stocks.getOrDefault(clau, 0L),
                        pendentFixat, pendentAssignat, restant, excesPendent));
            }
        }

        // Grups amb stock a la plataforma i sense cap línia de traspàs on assignar-lo
        for (var stock : stocks.entrySet()) {
            if (!liniesPerGrup.containsKey(stock.getKey()) && stock.getValue() > 0) {
                descompensats.add(new GrupDescompensat(stock.getKey(), stock.getValue(), 0, 0, stock.getValue(), 0));
            }
        }

        if (!isSimulacio) {
            aplicar(canvis);
        }

        var resultat = new ResultatMigracio(isSimulacio,
                liniesPerGrup.size(),
                stocks.size(),
                (int) descompensats.stream().filter(GrupDescompensat::isCorregit).count(),
                (int) descompensats.stream().filter(GrupDescompensat::isNoReparable).count(),
                (int) descompensats.stream().filter(g -> g.stockNoAssignat() > 0).count(),
                (int) descompensats.stream().filter(g -> g.excesPendent() > 0).count(),
                canvis.size(),
                descompensats,
                canvis);
        System.out.println(LocalDateTime.now() + " Grups descompensats: " + descompensats.size()
                + " (corregits: " + resultat.grupsCorregits() + ", no reparables: " + resultat.grupsNoReparables() + ")"
                + " / línies " + (isSimulacio ? "a actualitzar: " : "actualitzades: ") + canvis.size());
        return resultat;
    }

    /**
     * Línies dels albarans de traspàs a plataforma agrupades per (empresa, magatzem plataforma,
     * article-client) i, dins de cada grup, en <b>ordre FIFO invers</b> (primer les més noves), que és
     * l'ordre en què s'ha d'assignar l'stock que queda per repartir.
     */
    private Map<ClauGrup, List<LiniaTraspas>> obtenirLiniesTraspasPlataforma() {
        String sql = """
                SELECT a.informacio_traspas ->> 'magatzemReceptor' AS magatzem,
                       l.empresa, l.codi_albara, l.linia, l.artint, l.clicod,
                       l.quantitat, l.quantitat_pendent_consumir
                FROM albarans.linia_albara l
                JOIN albarans.albara a ON a.empresa = l.empresa AND a.codi = l.codi_albara
                JOIN com_magatzem.magatzem m ON m.codi = a.informacio_traspas ->> 'magatzemReceptor'
                WHERE a.tipus = 'TRASPAS_PLATAFORMA'
                    AND m.tipus = 'PLATAFORMA'
                ORDER BY a.data DESC, a.codi DESC, l.linia DESC
                """;
        return jdbcAmes.query(sql, rs -> {
            Map<ClauGrup, List<LiniaTraspas>> resultat = new LinkedHashMap<>();
            while (rs.next()) {
                var clau = new ClauGrup(rs.getString("empresa"), rs.getString("magatzem"),
                        rs.getString("artint"), rs.getString("clicod"));
                resultat.computeIfAbsent(clau, k -> new ArrayList<>()).add(new LiniaTraspas(
                        rs.getLong("codi_albara"),
                        rs.getLong("linia"),
                        rs.getLong("quantitat"),
                        rs.getLong("quantitat_pendent_consumir")));
            }
            return resultat;
        });
    }

    /** Stock de les fitxes dels magatzems de tipus plataforma, per (empresa, magatzem, article-client). */
    private Map<ClauGrup, Long> obtenirStocksPlataforma() {
        String sql = """
                SELECT f.empresa, f.magatzem, f.artint, f.clicod, SUM(f.stock) AS stock
                FROM inventari.fitxa f
                JOIN com_magatzem.magatzem m ON m.codi = f.magatzem
                WHERE m.tipus = 'PLATAFORMA'
                GROUP BY 1, 2, 3, 4
                HAVING SUM(f.stock) > 0
                """;
        return jdbcAmes.query(sql, rs -> {
            Map<ClauGrup, Long> resultat = new LinkedHashMap<>();
            while (rs.next()) {
                resultat.put(new ClauGrup(rs.getString("empresa"), rs.getString("magatzem"),
                        rs.getString("artint"), rs.getString("clicod")), rs.getLong("stock"));
            }
            return resultat;
        });
    }

    /**
     * Actualitza únicament la columna del pendent de consumir de les línies afectades (no es fa servir
     * el repositori perquè el seu {@code save} reescriu tota la fila i en regenera l'ETag).
     */
    private void aplicar(List<CanviLinia> canvis) {
        String sql = """
                UPDATE albarans.linia_albara
                SET quantitat_pendent_consumir = ?
                WHERE empresa = ? AND codi_albara = ? AND linia = ?
                """;
        for (int i = 0; i < canvis.size(); i += MIDA_LOT) {
            var lot = canvis.subList(i, Math.min(i + MIDA_LOT, canvis.size())).stream()
                    .map(c -> new Object[] { c.pendentDespres(), c.grup().empresa(), c.codiAlbara(), c.linia() })
                    .toList();
            jdbcAmes.batchUpdate(sql, lot);
        }
    }

    /** Grup sobre el qual es comprova l'invariant: una peça d'una empresa en un magatzem plataforma. */
    public record ClauGrup(String empresa, String magatzem, String artint, String clicod) {}

    /** Línia d'un albarà de traspàs a plataforma amb la seva quantitat i el seu pendent de consumir. */
    private record LiniaTraspas(long codiAlbara, long linia, long quantitat, long pendentConsumir) {}

    /**
     * Grup on l'stock de la plataforma no coincidia amb el pendent de consumir dels seus traspassos.
     * Un mateix grup pot ser corregit i no reparable alhora (s'ha assignat part de l'stock i n'ha quedat
     * sense assignar).
     *
     * @param stock           stock actual de la fitxa del magatzem plataforma
     * @param pendentFixat    pendent de les línies que ja en tenien (migrat, no s'ha tocat)
     * @param pendentAssignat pendent assignat a les línies que estaven a zero
     * @param stockNoAssignat stock que no s'ha pogut assignar a cap línia de traspàs
     * @param excesPendent    excés del pendent migrat sobre l'stock (no es redueix, només s'informa)
     */
    public record GrupDescompensat(ClauGrup grup, long stock, long pendentFixat, long pendentAssignat,
                                   long stockNoAssignat, long excesPendent) {

        /** Cert si s'ha pogut assignar pendent a alguna línia que estava a zero. */
        public boolean isCorregit() {
            return pendentAssignat > 0;
        }

        /** Cert si el grup no queda quadrat: sobra stock sense assignar o el pendent migrat supera l'stock. */
        public boolean isNoReparable() {
            return stockNoAssignat > 0 || excesPendent > 0;
        }
    }

    /** Canvi del pendent de consumir d'una línia concreta d'un albarà de traspàs (sempre de zero a &gt; 0). */
    public record CanviLinia(ClauGrup grup, long codiAlbara, long linia, long quantitat, long pendentAbans, long pendentDespres) {}

    /**
     * @param grupsAmbTraspassos       grups (empresa, plataforma, article-client) amb línies de traspàs
     * @param grupsAmbStock            grups amb fitxa en un magatzem plataforma
     * @param grupsCorregits           grups on s'ha assignat pendent a línies que estaven a zero
     * @param grupsNoReparables        grups que no queden quadrats (suma dels dos motius següents, sense duplicats)
     * @param grupsStockSenseTraspas   grups amb stock que no s'ha pogut assignar a cap línia de traspàs
     * @param grupsPendentSuperiorStock grups on el pendent migrat supera l'stock (no es redueix)
     * @param liniesActualitzades      línies de traspàs amb el pendent de consumir omplert
     * @param descompensats            detall per grup de tot el que no quadrava
     * @param canvis                   detall del canvi de pendent línia a línia
     */
    public record ResultatMigracio(
            boolean isSimulacio,
            int grupsAmbTraspassos,
            int grupsAmbStock,
            int grupsCorregits,
            int grupsNoReparables,
            int grupsStockSenseTraspas,
            int grupsPendentSuperiorStock,
            int liniesActualitzades,
            List<GrupDescompensat> descompensats,
            List<CanviLinia> canvis) {}

}
