package ames.comercial.edi.beans;

public class CF {
	
	public static final int i_inicio = 2;
	public static final int i_nombreFacturacion = 37;
	public static final int i_departamentFacturacion = 72;
	
	private String inicio;
	private String nombreFacturacion;
	private String departamentFacturacion;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getNombreFacturacion() {
		return nombreFacturacion;
	}

	public void setNombreFacturacion(String nombreFacturacion) {
		this.nombreFacturacion = nombreFacturacion;
	}

	public String getDepartamentFacturacion() {
		return departamentFacturacion;
	}

	public void setDepartamentFacturacion(String departamentFacturacion) {
		this.departamentFacturacion = departamentFacturacion;
	}

	@Override
	public String toString() {
		return "CF [inicio=" + inicio + ", nombreFacturacion=" + nombreFacturacion + ", departamentFacturacion="
				+ departamentFacturacion + "]";
	}

	public CF() {
	}

}
