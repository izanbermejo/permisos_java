package ames.comercial.edi.beans;

import java.util.List;

public class ComandaMissatgeEDI {
//	String nombreFicheroEDI;
	CA CA;
	CB CB;
	CC CC;
	CD CD;
	CI CI;
	CP CP;
	CQ CQ;
	CF CF;
	List<CT> textosLibres;
	ZZ ZZ;
	List<Linea> lineas;

//	public String getNombreFicheroEDI() {
//		return nombreFicheroEDI;
//	}
//
//	public void setNombreFicheroEDI(String nombreFicheroEDI) {
//		this.nombreFicheroEDI = nombreFicheroEDI;
//	}

	public CA getCA() {
		return CA;
	}

	public void setCA(CA cA) {
		CA = cA;
	}

	public CB getCB() {
		return CB;
	}

	public void setCB(CB cB) {
		CB = cB;
	}

	public CC getCC() {
		return CC;
	}

	public void setCC(CC cC) {
		CC = cC;
	}

	public CD getCD() {
		return CD;
	}

	public void setCD(CD cD) {
		CD = cD;
	}

	public CI getCI() {
		return CI;
	}

	public void setCI(CI cI) {
		CI = cI;
	}

	public CP getCP() {
		return CP;
	}

	public void setCP(CP cP) {
		CP = cP;
	}

	public CQ getCQ() {
		return CQ;
	}

	public void setCQ(CQ cQ) {
		CQ = cQ;
	}

	public CF getCF() {
		return CF;
	}

	public void setCF(CF cF) {
		CF = cF;
	}

	public ZZ getZZ() {
		return ZZ;
	}

	public void setZZ(ZZ zZ) {
		ZZ = zZ;
	}

	public List<Linea> getLineas() {
		return lineas;
	}

	public void setLineas(List<Linea> lineas) {
		this.lineas = lineas;
	}

	public List<CT> getTextosLibres() {
		return textosLibres;
	}

	public void setTextosLibres(List<CT> textosLibres) {
		this.textosLibres = textosLibres;
	}

	public ComandaMissatgeEDI(CA CA, CB CB, CC CC, CD CD, CI CI, CP CP, CQ CQ, CF CF, List<CT> textosLibres, ZZ ZZ, List<Linea> lineas) {
		this.CA = CA;
		this.CB = CB;
		this.CC = CC;
		this.CD = CD;
		this.CI = CI;
		this.CP = CP;
		this.CQ = CQ;
		this.CF = CF;
		this.textosLibres = textosLibres;
		this.ZZ = ZZ;
		this.lineas = lineas;
	}

	public ComandaMissatgeEDI() {

	}
}
