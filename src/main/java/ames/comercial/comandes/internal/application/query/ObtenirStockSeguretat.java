package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.internal.ObtenirArticleClientInformacioComanda;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.server.ETag;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ObtenirStockSeguretat {

	@Autowired ObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;
	@Autowired LiniaComandaRepository liniaComandaRepo;
	@Autowired ComandaRepository comandaRepo;

	public Optional<ObtenirStockSeguretatResponse> executar (String articleClientParam) {
		// Obtenció de l'articleclient a partir dels 13 dígits
		var articleClient = obtenirArticleClientInformacioComanda.executar(articleClientParam).orElseThrow(() -> new ArticleClientNotFound(articleClientParam));
		// Creació de la clau articleclient
		var clauArticleClient = KeyArticleClient.of(articleClient.artint(), articleClient.codiClient());

		// Obtenció de la línia de comanda de seguretat (si existeix) i del seu ETAG (ja que farà
		// falta per l'usuari, la data i la versió)
		var optLiniaComanda = liniaComandaRepo.findStockSeguretat(clauArticleClient);
		if (optLiniaComanda.isEmpty())
			return Optional.empty();
		var liniaComanda = optLiniaComanda.get();
		var etagLiniaComanda = liniaComandaRepo.etag(liniaComanda.comanda(), liniaComanda.numero());

		// Obtenció de la comanda
		var comanda = comandaRepo.find(liniaComanda.comanda()).orElseThrow(ComandaNoExisteix::new);

		// Construcció de la resposta
		return Optional.of(ObtenirStockSeguretatResponseImpl.builder()
						.dataSolicitada(liniaComanda.dataSolicitada())
						.quantitat(liniaComanda.quantitat())
						.stockSeguretatClient(TipusLiniaComanda.STOCK_SEG_CLIENT.equals(liniaComanda.tipus()))
						.usuari(etagLiniaComanda.map(ETag::usuari).orElse(""))
						.datareg(etagLiniaComanda.map(ETag::data).orElse(LocalDateTime.now()))
						.versio(etagLiniaComanda.map(ETag::versio).orElse(""))
				.build());
    }

	@JsonDeserialize(builder = ObtenirStockSeguretatResponseImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface ObtenirStockSeguretatResponse {
		LocalDate dataSolicitada();
		long quantitat();
		boolean stockSeguretatClient();
		String usuari();
		LocalDateTime datareg();
		String versio();
	}

}
