package ames.comercial.comandes.service;

import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;
import ames.comercial.shared.TipusArticleClient;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculPotServirLiniaNormalitzat implements ICalculPotServirLiniaNormalitzat {

	Map<KeyArticleClient, Stock> mapStocks;
	LocalDate dataMaximaReserva;
	Map<KeyArticleClient, TipusArticleClient> mapTipus;

	public CalculPotServirLiniaNormalitzat(Map<KeyArticleClient, Stock> mapStocks, Map<KeyArticleClient, TipusArticleClient> mapTipus,
										   long diesReserva) {
		this.mapStocks = mapStocks;
		this.mapTipus = mapTipus;
		this.dataMaximaReserva = RequestThread.dateLocal().plusDays(diesReserva);
	}

	@Override
	public HashMap<Long, CalculServibleResp> calcul(List<LiniaNormalitzatReq> linies) {
		var resultat = new HashMap<Long, CalculServibleResp>();

		// Ordenació de les línies per data ja que afecta a l'stock que es va comptant com a reserva
		List<LiniaNormalitzatReq> liniesOrdenades = linies.stream()
				.sorted(Comparator.comparing(LiniaNormalitzatReq::dataSolicitada))
				.toList();
		// Càlcul per cada línia tenint en compte si es tracta d'una peça especial o no
		liniesOrdenades.forEach(l -> {
			var tipus = mapTipus.get(l.articleClient());
			if (TipusArticleClient.ESPECIAL == tipus) {
				resultat.put(l.linia(), calculEspecial(l.articleClient()));
			} else if (TipusArticleClient.IBINSA == tipus) {
				resultat.put(l.linia(), calculEspecial(l.articleClient()));
			} else {
				resultat.put(l.linia(), calculNormalitzat(l.articleClient(), l.quantitat(), l.dataSolicitada()));
			}
		});
		return resultat;
	}

	private long stockDisponible (KeyArticleClient articleClient) {
		var stock = mapStocks.get(articleClient);
		return stock != null ? stock.stock() : 0;
	}

	private CalculServibleResp calculEspecial (KeyArticleClient articleClient) {
		return new CalculServibleResp(Reservable.NO_APLICA, 0, stockDisponible(articleClient));
	}

	private CalculServibleResp calculNormalitzat (KeyArticleClient articleClient, long quantitat, LocalDate dataSolicitada) {
		// En cas que la data solicitada sigui posterior a la data màxima de reserva
		if (dataSolicitada.isAfter(dataMaximaReserva))
			return new CalculServibleResp(Reservable.NO, 0, stockDisponible(articleClient));
		var stockReservat = reservaStock(articleClient, quantitat);
		var reservable = Reservable.of(stockReservat, quantitat);
		return new CalculServibleResp(reservable, stockReservat, stockDisponible(articleClient));
	}

	private long reservaStock(KeyArticleClient artint, long unitats) {
		var stock = mapStocks.get(artint);
		if (stock != null) {
			var unitatsReservades = Math.min(stock.stockDisponible(), unitats);
			mapStocks.put(artint, new Stock(stock.stock(), stock.reservat()+unitatsReservades));
			return unitatsReservades;
		}
		return 0;
	}

}
