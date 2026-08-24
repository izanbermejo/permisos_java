package ames.comercial.edi.beans;

public class LQ {

	public static final int i_inicio = 2;
	public static final int i_cantidadBalance = 17;
	public static final int i_fechaCantidadBalance = 27;
	public static final int i_cantidadAtraso = 42;
	public static final int i_fechaCantidadAtraso = 52;
	public static final int i_cantidadUrgente = 67;
	public static final int i_fechaCantidadUrgente = 77;
	public static final int i_cantidadTransito = 92;
	public static final int i_fechaCantidadTransito = 102;
	public static final int i_cantidadAcumuladaRecibida = 117;
	public static final int i_cantidadAcumuladaProgramada = 132;
	public static final int i_inicioPeriodoAcumulada = 142;
	public static final int i_finPeriodoAcumulada = 152;
	public static final int i_cantidadAcumuladaPeriodoAnterior = 167;

	private String inicio;
	private String cantidadBalance;
	private String fechaCantidadBalance;
	private String cantidadAtraso;
	private String fechaCantidadAtraso;
	private String cantidadUrgente;
	private String fechaCantidadUrgente;
	private String cantidadTransito;
	private String fechaCantidadTransito;
	private String cantidadAcumuladaRecibida;
	private String cantidadAcumuladaProgramada;
	private String inicioPeriodoAcumulada;
	private String finPeriodoAcumulada;
	private String cantidadAcumuladaPeriodoAnterior;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getCantidadBalance() {
		return cantidadBalance;
	}

	public void setCantidadBalance(String cantidadBalance) {
		this.cantidadBalance = cantidadBalance;
	}

	public String getFechaCantidadBalance() {
		return fechaCantidadBalance;
	}

	public void setFechaCantidadBalance(String fechaCantidadBalance) {
		this.fechaCantidadBalance = fechaCantidadBalance;
	}

	public String getCantidadAtraso() {
		return cantidadAtraso;
	}

	public void setCantidadAtraso(String cantidadAtraso) {
		this.cantidadAtraso = cantidadAtraso;
	}

	public String getFechaCantidadAtraso() {
		return fechaCantidadAtraso;
	}

	public void setFechaCantidadAtraso(String fechaCantidadAtraso) {
		this.fechaCantidadAtraso = fechaCantidadAtraso;
	}

	public String getCantidadUrgente() {
		return cantidadUrgente;
	}

	public void setCantidadUrgente(String cantidadUrgente) {
		this.cantidadUrgente = cantidadUrgente;
	}

	public String getFechaCantidadUrgente() {
		return fechaCantidadUrgente;
	}

	public void setFechaCantidadUrgente(String fechaCantidadUrgente) {
		this.fechaCantidadUrgente = fechaCantidadUrgente;
	}

	public String getCantidadTransito() {
		return cantidadTransito;
	}

	public void setCantidadTransito(String cantidadTransito) {
		this.cantidadTransito = cantidadTransito;
	}

	public String getFechaCantidadTransito() {
		return fechaCantidadTransito;
	}

	public void setFechaCantidadTransito(String fechaCantidadTransito) {
		this.fechaCantidadTransito = fechaCantidadTransito;
	}

	public String getCantidadAcumuladaRecibida() {
		return cantidadAcumuladaRecibida;
	}

	public void setCantidadAcumuladaRecibida(String cantidadAcumuladaRecibida) {
		this.cantidadAcumuladaRecibida = cantidadAcumuladaRecibida;
	}

	public String getCantidadAcumuladaProgramada() {
		return cantidadAcumuladaProgramada;
	}

	public void setCantidadAcumuladaProgramada(String cantidadAcumuladaProgramada) {
		this.cantidadAcumuladaProgramada = cantidadAcumuladaProgramada;
	}

	public String getInicioPeriodoAcumulada() {
		return inicioPeriodoAcumulada;
	}

	public void setInicioPeriodoAcumulada(String inicioPeriodoAcumulada) {
		this.inicioPeriodoAcumulada = inicioPeriodoAcumulada;
	}

	public String getFinPeriodoAcumulada() {
		return finPeriodoAcumulada;
	}

	public void setFinPeriodoAcumulada(String finPeriodoAcumulada) {
		this.finPeriodoAcumulada = finPeriodoAcumulada;
	}

	public String getCantidadAcumuladaPeriodoAnterior() {
		return cantidadAcumuladaPeriodoAnterior;
	}

	public void setCantidadAcumuladaPeriodoAnterior(String cantidadAcumuladaPeriodoAnterior) {
		this.cantidadAcumuladaPeriodoAnterior = cantidadAcumuladaPeriodoAnterior;
	}
	@Override
	public String toString() {
		return "LQ{" +
				"inicio='" + inicio + '\'' +
				", cantidadBalance='" + cantidadBalance + '\'' +
				", fechaCantidadBalance='" + fechaCantidadBalance + '\'' +
				", cantidadAtraso='" + cantidadAtraso + '\'' +
				", fechaCantidadAtraso='" + fechaCantidadAtraso + '\'' +
				", cantidadUrgente='" + cantidadUrgente + '\'' +
				", fechaCantidadUrgente='" + fechaCantidadUrgente + '\'' +
				", cantidadTransito='" + cantidadTransito + '\'' +
				", fechaCantidadTransito='" + fechaCantidadTransito + '\'' +
				", cantidadAcumuladaRecibida='" + cantidadAcumuladaRecibida + '\'' +
				", cantidadAcumuladaProgramada='" + cantidadAcumuladaProgramada + '\'' +
				", inicioPeriodoAcumulada='" + inicioPeriodoAcumulada + '\'' +
				", finPeriodoAcumulada='" + finPeriodoAcumulada + '\'' +
				", cantidadAcumuladaPeriodoAnterior='" + cantidadAcumuladaPeriodoAnterior + '\'' +
				'}';
	}

	public LQ() {
		super();
	}
}
