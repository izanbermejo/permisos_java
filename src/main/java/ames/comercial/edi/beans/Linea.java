package ames.comercial.edi.beans;

import java.util.List;

public class Linea {

	LA LA;
	LB LB;
	LC LC;
	LD LD;
	LS LS;
	List<LE> empaquetamientos;
	List<LT> observaciones;
	LG LG;
	LH LH;
	LI LI;
	List<LL> etiquetas;
	List<LineaPedidoPrevio> lineasPedidoPrevio;
	List<AlbaranPrevio> albaranesPrevios;
	List<Detalle> detalles;

	public LA getLA() {
		return LA;
	}

	public void setLA(LA LA) {
		this.LA = LA;
	}

	public LB getLB() {
		return LB;
	}

	public void setLB(LB LB) {
		this.LB = LB;
	}

	public LC getLC() {
		return LC;
	}

	public void setLC(LC LC) {
		this.LC = LC;
	}

	public LD getLD() {
		return LD;
	}

	public void setLD(LD LD) {
		this.LD = LD;
	}

	public LS getLS() {
		return LS;
	}

	public void setLS(LS LS) {
		this.LS = LS;
	}

	public List<LE> getEmpaquetamientos() {
		return empaquetamientos;
	}

	public void setEmpaquetamientos(List<LE> empaquetamientos) {
		this.empaquetamientos = empaquetamientos;
	}

	public List<LT> getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(List<LT> observaciones) {
		this.observaciones = observaciones;
	}

	public LG getLG() {
		return LG;
	}

	public void setLG(LG LG) {
		this.LG = LG;
	}

	public LH getLH() {
		return LH;
	}

	public void setLH(LH LH) {
		this.LH = LH;
	}

	public LI getLI() {
		return LI;
	}

	public void setLI(LI LI) {
		this.LI = LI;
	}

	public List<LL> getEtqiquetas() {
		return etiquetas;
	}

	public void setEtiquetas(List<LL> etiquetas) {
		this.etiquetas = etiquetas;
	}

	public List<LineaPedidoPrevio> getLineasPedidoPrevio() {
		return lineasPedidoPrevio;
	}

	public void setLineasPedidoPrevio(List<LineaPedidoPrevio> lineasPedidoPrevio) {
		this.lineasPedidoPrevio = lineasPedidoPrevio;
	}

	public List<AlbaranPrevio> getAlbaranesPrevios() {
		return albaranesPrevios;
	}

	public void setAlbaranesPrevios(List<AlbaranPrevio> albaranesPrevios) {
		this.albaranesPrevios = albaranesPrevios;
	}

	public List<Detalle> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<Detalle> detalles) {
		this.detalles = detalles;
	}
}
