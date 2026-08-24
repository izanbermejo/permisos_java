package ames.comercial.comandes.service;

import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.TarifesCalculadoraReq;
import ames.comercial.comandes.service.response.*;
import ames.comercial.costtransport.ICalcularCostTransport;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class CalculadoraTarifaPesa {

	@Autowired CalculadoraComandaNormalitzat calculadora;
	@Autowired IProviderTarifaActual obtenirTarifaActual;
	@Autowired ICalcularCostTransport calcularCostTransport;

	public CalculTarifaPesaResponse calcula (String codiClient, List<LiniaNormalitzatReq> linies, BigDecimal importTotalNetActual,
											 BigDecimal importTotalBrutActual, BigDecimal pesTotalActual, BigDecimal incrementPesTransport) {

		// Obtenció del client
		var client = new ObtenirClientAds().get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
		// Empresa del client
		var empresa = Empresa.getByClau(client.empresa());
		var tarifesActualClient = obtenirTarifaActual.executar(codiClient);

		var calcul = calculadora.calcula(client, empresa, TarifesCalculadoraReq.from(tarifesActualClient), linies);

		var pesKg = pesTotalActual.add(calcul.pes());
		var importTotalNet = importTotalNetActual.add(calcul.importNet());
		var importTotalBrut = importTotalBrutActual.add(calcul.importBrut());
		var costTransport = calcularCostTransport.calcula(client.clicod(), importTotalNet, pesKg, client.incoterm());
		var costTransportAfegit = calcularCostTransport.calcula(client.clicod(), importTotalNet, pesKg.multiply(incrementPesTransport), client.incoterm());

		return CalculTarifaPesaResponseImpl.builder()
				.tarifaCoixinets(calcul.tarifaCoixinets())
				.tarifaBarres(calcul.tarifaBarres())
				.tarifaMedical(calcul.tarifaMedical())
				.tarifaIbinsa(calcul.tarifaIbinsa())
				.tarifaFiltresBxx(calcul.tarifaFiltresBxx())
				.tarifaFiltresSsu(calcul.tarifaFiltresSsu())
				.tarifaFiltresSxx(calcul.tarifaFiltresSxx())
				.tarifaFiltresSsuPlaques(calcul.tarifaFiltresSsuPlaques())
				.addAllLinies(calcul.linies())
				.costTransport(costTransport)
				.costTransportAfegit(costTransportAfegit)
				.pesTotal(pesKg)
				.importTotalNet(importTotalNet)
				.importTotalBrut(importTotalBrut)
				.build();
	}
	
}
