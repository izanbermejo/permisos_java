package ames.comercial.comandes;

public class ReservarLiniaServiceTest {

//    KeyArticleClient keyArticleClient = KeyArticleClient.of("123456", "123456");
//
//    public IObtenirStocksAds stock(long quantitat) {
//        return new IObtenirStocksAds() {
//            @Override
//            public Optional<Stock> query(KeyArticleClient article) {
//                if (quantitat < 0) return Optional.empty();
//                else if (quantitat == 0) return Optional.of(new Stock(0, 0));
//                else return Optional.of(new Stock(quantitat, 0));
//            }
//            @Override
//            public Map<KeyArticleClient, Stock> query(Set<KeyArticleClient> articles) {
//                return Map.of();
//            }
//        };
//    }
//
//    public ReservarLiniaServiceRequest req(KeyLiniaComanda keyLinia, long quantitat, LocalDate data) {
//        return new CalculadoraReserves.ReservarLiniaServiceRequest(keyLinia, quantitat, data);
//    }
//
//    @Test
//    @DisplayName("Quan no es troba stock de la peça no es retorna informació de cap reserva")
//    public void stockBuitSenseReserva(){
//        var obtenirStocksBuit = stock(-1);
//        var req = req(KeyLiniaComanda.of(1, 1), 500, LocalDate.now());
//        var resp = new CalculadoraReserves(obtenirStocksBuit, keyArticleClient, req, List.of()).executar();
//        assertAll(() -> {
//            assertEquals(1, resp.size());
//            assertEquals(Reservable.RES, resp.get(0).infoReserva().estat());
//            assertEquals(0, resp.get(0).infoReserva().quantitat());
//        });
//    }
//
//    @Test
//    @DisplayName("Només es reserva la sol·licitada de manera parcial quan l'stock es inferior a la quantitat sol·licitada")
//    public void reservaParcialSolicitada(){
//        var stock = stock(300);
//        var req = new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(1, 1), 500, LocalDate.now());
//        var listReq = List.of(new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(2, 1), 500, LocalDate.now()));
//        var resp = new CalculadoraReserves(stock, keyArticleClient, req, listReq).executar();
//        assertAll(() -> {
//            assertEquals(2, resp.size());
//            assertEquals(Reservable.PARCIAL, resp.get(0).infoReserva().estat());
//            assertEquals(300, resp.get(0).infoReserva().quantitat());
//            assertEquals(Reservable.RES, resp.get(1).infoReserva().estat());
//            assertEquals(0, resp.get(1).infoReserva().quantitat());
//        });
//    }
//
//    @Test
//    @DisplayName("Es reserva la totalitat sol·licitada i cap mes quan l'stock es just el que queda")
//    public void reservaTotSolicitada(){
//        var stock = stock(300);
//        var req = new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(1, 1), 300, LocalDate.now());
//        var listReq = List.of(new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(2, 1), 500, LocalDate.now()));
//        var resp = new CalculadoraReserves(stock, keyArticleClient, req, listReq).executar();
//        assertAll(() -> {
//            assertEquals(2, resp.size());
//            assertEquals(Reservable.TOT, resp.get(0).infoReserva().estat());
//            assertEquals(300, resp.get(0).infoReserva().quantitat());
//            assertEquals(Reservable.RES, resp.get(1).infoReserva().estat());
//            assertEquals(0, resp.get(1).infoReserva().quantitat());
//        });
//    }
//
//    @Test
//    @DisplayName("Es reserva la totalitat sol·licitada i parcialment la següent")
//    public void reservaTotSolicitadaIParcialmentAltres(){
//        var stock = stock(500);
//        var req = new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(1, 1), 300, LocalDate.now());
//        var listReq = List.of(new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(2, 1), 500, LocalDate.now()),
//                new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(3, 1), 500, LocalDate.now()));
//        var resp = new CalculadoraReserves(stock, keyArticleClient, req, listReq).executar();
//        var res1 = resp.get(0);
//        var res2 = resp.get(1);
//        assertAll(() -> {
//            assertEquals(Reservable.TOT, res1.infoReserva().estat());
//            assertEquals(300, res1.infoReserva().quantitat());
//            assertEquals(Reservable.PARCIAL, res2.infoReserva().estat());
//            assertEquals(200, res2.infoReserva().quantitat());
//        });
//    }
//
//    @Test
//    @DisplayName("Es reserva la totalitat sol·licitada i parcialment la següent tenint en compte la data sol·licitada i la comanda")
//    public void reservaTotSolicitadaIParcialmentAltresPerData(){
//        var stock = stock(500);
//        var req = new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(1, 1), 300, LocalDate.now());
//        var listReq = List.of(new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(2, 1), 500, LocalDate.now().plusDays(2)),
//                new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(8, 1), 500, LocalDate.now()),
//                new CalculadoraReserves.ReservarLiniaServiceRequest(KeyLiniaComanda.of(3, 1), 500, LocalDate.now()));
//        var resp = new CalculadoraReserves(stock, keyArticleClient, req, listReq).executar();
//        var res1 = resp.get(0);
//        var res2 = resp.get(1);
//        assertAll(() -> {
//            assertEquals(Reservable.TOT, res1.infoReserva().estat());
//            assertEquals(300, res1.infoReserva().quantitat());
//            assertEquals(3, res2.clauLinia().comanda());
//        });
//    }

}
