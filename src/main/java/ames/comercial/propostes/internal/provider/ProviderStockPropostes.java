package ames.comercial.propostes.internal.provider;

import ames.comercial.cache.stocksatelit.StockSatelitService;
import ames.comercial.cache.stocksatelit.StockSatelitService.RegStockSatelit;
import ames.comercial.inventari.ext.IObtenirStockMagatzemsIntermig;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProviderStockPropostes implements IProviderStockPropostes {

    StockSatelitService stockSatelitService;
    IObtenirStockMagatzemsIntermig obtenirStockMagatzemsIntermig;

    public ProviderStockPropostes (StockSatelitService stockSatelitService,
                                   IObtenirStockMagatzemsIntermig obtenirStockMagatzemsIntermig) {
        this.stockSatelitService = stockSatelitService;
        this.obtenirStockMagatzemsIntermig = obtenirStockMagatzemsIntermig;
    }

    @Override
    public IProviderStockPropostesResponse provide(List<String> listClients) {
        return new ProviderStockPropostesReponse(obtenirRegistresArtfit(listClients),
                stockSatelitService.obtenirRegistresStockSatelit());
    }

    private List<RegArtfit> obtenirRegistresArtfit(List<String> listClients) {
        // L'stock rellevant per decidir un traspàs és l'stock REAL disponible al magatzem final: el
        // que ja hi ha al magatzem MÉS el que està en trànsit als seus magatzems intermitjos. El
        // servei d'inventari ja retorna aquest total agregat (stockTotal) per magatzem, així que
        // aquí només cal:
        //   - descartar les files que corresponen a un magatzem intermig (isIntermig), perquè el seu
        //     stock ja queda comptat dins del stockTotal del seu magatzem final (evitem comptar-lo
        //     dues vegades),
        //   - quedar-nos amb les files amb stock disponible.
        return obtenirStockMagatzemsIntermig.perClients(listClients)
                .stream()
                .filter(reg -> !reg.isIntermig())
                .filter(reg -> reg.stockTotal() > 0)
                .map(reg -> new RegArtfit(reg.artint(), reg.clicod(), reg.empresa(), reg.magatzem(),
                        (int) reg.stockTotal()))
                .toList();
    }

    public static class ProviderStockPropostesReponse implements IProviderStockPropostesResponse {

        List<RegArtfit> regArtfits;
        List<RegStockSatelit> regStockSatelits;

        public ProviderStockPropostesReponse (List<RegArtfit> regArtfits, List<RegStockSatelit> regStockSatelits) {
            this.regArtfits = regArtfits;
            this.regStockSatelits = regStockSatelits;
        }

        @Override
        public int stock(KeyArticleClient articleClient, String empresa, String magatzem) {
            return regArtfit(articleClient, empresa, magatzem).map(RegArtfit::stock).orElse(0);
        }

        @Override
        public InformacioStockServir stockServir(KeyArticleClient articleClient, String empresa, String magatzem, int stockServir) {
            var optRegArtfit = regArtfit(articleClient, empresa, magatzem);
            // En cas que no existeixi el registre vol dir que no hi ha stock
            if (optRegArtfit.isEmpty())
                return InformacioStockServir.empty();
            // En cas que existeixi el registre cal veure si es pot servir tota la línia i descomptar
            // de l'stock per si hi ha més línies d'aquesta mateixa peça no comptar amb aquest stock
            var regArtfit = optRegArtfit.get();
            // L'stock que es pot servir és el mínim entre la qtat. pendent i l'stock disponible
            var stockServible = Math.min(regArtfit.stock, stockServir);
            regArtfit.descomptaStock(stockServible);
            return InformacioStockServirImpl.builder()
                    .stockDisponibleServir(stockServible)
                    .stockSatelit(stockSatelit(articleClient, magatzem))
                    .stockSobrantDespresServir(regArtfit.stock())
                    .build();
        }

        @Override
        public int stockSatelit(KeyArticleClient articleClient, String magatzem) {
            return regStockSatelits.stream()
                    .filter(r -> r.articleClient().equals(articleClient))
                    .filter(r -> r.magatzem().equals(magatzem))
                    .findAny()
                    .map(RegStockSatelit::stock)
                    .orElse(0);
        }

        private Optional<RegArtfit> regArtfit(KeyArticleClient articleClient, String empresa, String magatzem) {
            return regArtfits.stream()
                    .filter(r -> r.artint.equals(articleClient.artint()))
                    .filter(r -> r.clicod.equals(articleClient.clicod()))
                    .filter(r -> r.empresa.equals(empresa))
                    .filter(r -> r.magatzem.equals(magatzem))
                    .findAny();
        }

    }

    private static class RegArtfit {
        String artint;
        String clicod;
        String empresa;
        String magatzem;
        int stock;

        public RegArtfit (String artint, String clicod, String empresa, String magatzem, int stock) {
            this.artint = artint;
            this.clicod = clicod;
            this.empresa = empresa;
            this.magatzem = magatzem;
            this.stock = stock;
        }

        public String artint() { return artint; }
        public String clicod() { return  clicod; }
        public String empresa() { return empresa; }
        public String magatzem() { return magatzem; }
        public int stock() { return stock; }

        public void descomptaStock(int qtat) {
            this.stock -= qtat;
        }
    }

}
