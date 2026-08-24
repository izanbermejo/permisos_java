package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.comandes.ComandesException.ProgramaNoDisponibleNormalitzats;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.query.QueryRepository;
import ames.comercial.comandes.response.ItemLiniaComandaImpl;
import ames.comercial.comandes.response.ObtenirLiniesComandaResponse;
import ames.comercial.comandes.response.ObtenirLiniesComandaResponseImpl;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObtenirLiniesComanda {
	
	QueryRepository queryRepo;
	IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;
	
	public ObtenirLiniesComanda(QueryRepository queryRepo,
								IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda) {
		this.queryRepo = queryRepo;
		this.obtenirArticleClientInformacioComanda = obtenirArticleClientInformacioComanda;
	}

	public ObtenirLiniesComandaResponse executar (String articleClient, LocalDate dataInicial, LocalDate dataFinal,
												  boolean ocultarServides, boolean mostrarOrdreAscendent) {
		var info = obtenirArticleClientInformacioComanda.executar(articleClient).orElseThrow(() -> new ArticleClientNotFound(articleClient));
		// En cas que sigui una peça de normalitzats no es permet veure el programa
		if (info.codiClient().equals("000000"))
			throw new ProgramaNoDisponibleNormalitzats();
		// Albarans servits de totes les línies (una sola consulta al nou sistema, no per línia)
		var albaransPerLinia = queryRepo.searchAlbaransServitsPerLinia(KeyArticleClient.of(info.artint(), info.codiClient()));
		// Obtenció de les línies, adjuntant-hi els seus albarans, i filtrat en cas que es vulguin amagar les servides
		var linies = queryRepo.searchLiniesPeriode(info.artint(), info.codiClient(), dataInicial, dataFinal)
				.stream()
				.filter(l -> !(ocultarServides && l.servida())) // Oculta servides en cas que ocultarServides=true
				.map(l -> ItemLiniaComandaImpl.builder()
						.from(l)
						.albarans(albaransPerLinia.getOrDefault(KeyLiniaComanda.of(l.codi(), l.numero()), List.of()))
						.build())
				.collect(Collectors.toCollection(ArrayList::new));
		// Reverse en cas que es vulgui veure de forma descendent per data
		if (!mostrarOrdreAscendent)
			Collections.reverse(linies);
		return ObtenirLiniesComandaResponseImpl.builder()
				.info(info)
				.linies(linies)
				.build();
	}
	
}
