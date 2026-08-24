package ames.comercial.edi.beans;

public class LI {

	public static final int i_inicio=2;
	public static final int i_cambioIngenieria=37;
	public static final int i_fechaCambioIngerieria=47;
	public static final int i_numeroRuta=82;
	public static final int i_numeroSufijoRuta=117;
	public static final int i_numeroTransporte=152;

	private String inicio;
	private String cambioIngenieria;
	private String fechaCambioIngerieria;
	private String numeroRuta;
	private String numeroSufijoRuta;
	private String numeroTransporte;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getCambioIngenieria() {
		return cambioIngenieria;
	}

	public void setCambioIngenieria(String cambioIngenieria) {
		this.cambioIngenieria = cambioIngenieria;
	}

	public String getFechaCambioIngerieria() {
		return fechaCambioIngerieria;
	}

	public void setFechaCambioIngerieria(String fechaCambioIngerieria) {
		this.fechaCambioIngerieria = fechaCambioIngerieria;
	}

	public String getNumeroRuta() {
		return numeroRuta;
	}

	public void setNumeroRuta(String numeroRuta) {
		this.numeroRuta = numeroRuta;
	}

	public String getNumeroSufijoRuta() {
		return numeroSufijoRuta;
	}

	public void setNumeroSufijoRuta(String numeroSufijoRuta) {
		this.numeroSufijoRuta = numeroSufijoRuta;
	}

	public String getNumeroTransporte() {
		return numeroTransporte;
	}

	public void setNumeroTransporte(String numeroTransporte) {
		this.numeroTransporte = numeroTransporte;
	}

	@Override
	public String toString() {
		return "LI{" +
				"inicio='" + inicio + '\'' +
				", cambioIngenieria='" + cambioIngenieria + '\'' +
				", fechaCambioIngerieria='" + fechaCambioIngerieria + '\'' +
				", numeroRuta='" + numeroRuta + '\'' +
				", numeroSufijoRuta='" + numeroSufijoRuta + '\'' +
				", numeroTransporte='" + numeroTransporte + '\'' +
				'}';
	}

	public LI() {
		super();
	}
}
