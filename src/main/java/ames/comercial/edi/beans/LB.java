package ames.comercial.edi.beans;

public class LB {

	public static final int i_inicio = 2;
	public static final int i_stockActual = 17;
	public static final int i_stockSeguridad = 32;
	public static final int i_idArticuloProveedor = 67;
	public static final int i_paisConsignatario = 70;
	public static final int i_precio = 85;
	public static final int i_divisa = 88;

	private String inicio;
	private String stockActual;
	private String stockSeguridad;
	private String idArticuloProveedor;
	private String paisConsignatario;
	private String precio;
	private String divisa;


	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getStockActual() {
		return stockActual;
	}

	public void setStockActual(String stockActual) {
		this.stockActual = stockActual;
	}

	public String getStockSeguridad() {
		return stockSeguridad;
	}

	public void setStockSeguridad(String stockSeguridad) {
		this.stockSeguridad = stockSeguridad;
	}

	public String getIdArticuloProveedor() {
		return idArticuloProveedor;
	}

	public void setIdArticuloProveedor(String idArticuloProveedor) {
		this.idArticuloProveedor = idArticuloProveedor;
	}

	public String getPaisConsignatario() {
		return paisConsignatario;
	}

	public void setPaisConsignatario(String paisConsignatario) {
		this.paisConsignatario = paisConsignatario;
	}

	public String getPrecio() {
		return precio;
	}

	public void setPrecio(String precio) {
		this.precio = precio;
	}

	public String getDivisa() {
		return divisa;
	}

	public void setDivisa(String divisa) {
		this.divisa = divisa;
	}

	@Override
	public String toString() {
		return "LB{" +
				"inicio='" + inicio + '\'' +
				", stockActual='" + stockActual + '\'' +
				", stockSeguridad='" + stockSeguridad + '\'' +
				", idArticuloProveedor='" + idArticuloProveedor + '\'' +
				", paisConsignatario='" + paisConsignatario + '\'' +
				", precio='" + precio + '\'' +
				", divisa='" + divisa + '\'' +
				'}';
	}

	public LB() {
	}

}
