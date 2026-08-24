package ames.comercial.comandes.internal.domain.comanda;

import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;

import java.util.Optional;

public class Comanda {

	long codi;
	TipusComanda tipus;
	DadesComanda dades;
	InformacioClient informacioClient;
	Adresa adresa;
	InformacioEnviament informacioEnviament;
	Optional<DadesNormalitzat> dadesNormalitzat;
	Optional<DadesEnviamentJustificant> dadesEnviamentJustificant;
	boolean stockSeguretat;
	int numAdjunts;
	Servible servible;
	boolean servida;
	String usuari;
	
	public Comanda (long codi, ComandaProps props) {
		this.codi = codi;
		this.tipus = props.tipus();
		this.dades = props.dades();
		this.informacioClient = props.informacioClient();
		this.adresa = props.adresa();
		this.informacioEnviament = props.informacioEnviament();
		this.dadesNormalitzat = props.dadesNormalitzat();
		this.dadesEnviamentJustificant = props.dadesEnviamentJustificant();
		this.stockSeguretat = props.stockSeguretat();
		this.numAdjunts = props.numAdjunts();
		this.servida = props.servida();
		this.servible = props.servible();
		this.usuari = props.usuari();
	}
	
	public long codi() {
		return codi;
	}
	
	public TipusComanda tipus() {
		return tipus;
	}
	
	public String codiFormat () {
		return String.format("%07d", codi); 
	}
	
	public DadesComanda dades() {
		return dades;
	}
	
	public InformacioClient informacioClient() {
		return informacioClient;
	}

	public Adresa adresa() { return  adresa; }

	public InformacioEnviament informacioEnviament() { return informacioEnviament; }
	
	public Optional<DadesNormalitzat> dadesNormalitzat() {
		return dadesNormalitzat;
	}

	public Optional<DadesEnviamentJustificant> dadesEnviamentJustificant() { return dadesEnviamentJustificant; }

	public boolean isStockSeguretat() { return stockSeguretat; }

	public int numAdjunts() { return numAdjunts; }
	
	public boolean servida() {
		return servida;
	}

	public String usuari() { return usuari; }

	public Servible servible() { return servible; }
	
	public void marcarServida() {
		this.servible = Servible.NO_APLICA;
		this.servida = true;
	}
	
	public void marcarPendent() {
		this.servida = false;
	}

	public void canviarAdresa(Adresa adresa) { this.adresa = adresa; }

	public void canviarInformacioEnviament(InformacioEnviament informacioEnviament) { this.informacioEnviament = informacioEnviament; }

	public void canviarServible(Servible servible) { this.servible = servible; }

	public void canviarDadesNormalitzat(DadesNormalitzat dades) { this.dadesNormalitzat = Optional.ofNullable(dades); }

	public void afegirDadesEnviamentJustificant (DadesEnviamentJustificant dades) { this.dadesEnviamentJustificant = Optional.ofNullable(dades); }

    public void canviarInformacioClient(InformacioClient informacioClient) { this.informacioClient = informacioClient; }
}
