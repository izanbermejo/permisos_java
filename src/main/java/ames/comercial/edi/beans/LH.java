package ames.comercial.edi.beans;

public class LH {

	public static final int i_inicio=2;
	public static final int i_numeroPedidoPrevio =37;
	public static final int i_numeroLab=72;
	public static final int i_numeroLote=107;
	public static final int i_fechaLab=117;
	public static final int i_fechaPedidoPrevio=127;
	public static final int i_numeroPedidoNuevo=162;
	public static final int i_fechaPedidoNuevo=172;

	private String inicio;
	private String numPedidoPrevio;
	private String numeroLab;
	private String numeroLote;
	private String fechaLab;
	private String fechaPedidoPrevio;
	private String numeroPedidoNuevo;
	private String fechaPedidoNuevo;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getNumPedidoPrevio() {
		return numPedidoPrevio;
	}

	public void setNumPedidoPrevio(String numPedidoPrevio) {
		this.numPedidoPrevio = numPedidoPrevio;
	}

	public String getNumeroLab() {
		return numeroLab;
	}

	public void setNumeroLab(String numeroLab) {
		this.numeroLab = numeroLab;
	}

	public String getNumeroLote() {
		return numeroLote;
	}

	public void setNumeroLote(String numeroLote) {
		this.numeroLote = numeroLote;
	}

	public String getFechaLab() {
		return fechaLab;
	}

	public void setFechaLab(String fechaLab) {
		this.fechaLab = fechaLab;
	}

	public String getFechaPedidoNuevo() {
		return fechaPedidoNuevo;
	}

	public void setFechaPedidoNuevo(String fechaPedidoNuevo) {
		this.fechaPedidoNuevo = fechaPedidoNuevo;
	}

	public String getNumeroPedidoNuevo() {
		return numeroPedidoNuevo;
	}

	public void setNumeroPedidoNuevo(String numeroPedidoNuevo) {
		this.numeroPedidoNuevo = numeroPedidoNuevo;
	}

	@Override
	public String toString() {
		return "LH{" +
				"inicio='" + inicio + '\'' +
				", numPedidoPrevio='" + numPedidoPrevio + '\'' +
				", numeroLab='" + numeroLab + '\'' +
				", numeroLote='" + numeroLote + '\'' +
				", fechaLab='" + fechaLab + '\'' +
				", fechaPedidoPrevio='" + fechaPedidoNuevo + '\'' +
				", numeroPedidoPrevio='" + numeroPedidoNuevo + '\'' +
				", fechaPedidoPrevio='" + fechaPedidoNuevo + '\'' +
				'}';
	}

	public String getFechaPedidoPrevio() {
		return fechaPedidoPrevio;
	}

	public void setFechaPedidoPrevio(String fechaPedidoPrevio) {
		this.fechaPedidoPrevio = fechaPedidoPrevio;
	}

	public LH() {
		super();
	}
}
