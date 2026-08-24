package ames.comercial.edi.beans;

public class LD {

	public static final int i_inicio=2;
	public static final int i_direccionConsignatario=37;
	public static final int i_localidadConsignatario=72;
	public static final int i_provinciaConsignatario=97;
	public static final int i_codigoPostalConsignatario=107;
	public static final int i_faxConsignatario=142;

	private String inicio;
	private String direccionConsignatario;
	private String localidadConsignatario;
	private String provinciaConsignatario;
	private String codigoPostalConsignatario;
	private String faxConsignatario;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getDireccionConsignatario() {
		return direccionConsignatario;
	}

	public void setDireccionConsignatario(String direccionConsignatario) {
		this.direccionConsignatario = direccionConsignatario;
	}

	public String getLocalidadConsignatario() {
		return localidadConsignatario;
	}

	public void setLocalidadConsignatario(String localidadConsignatario) {
		this.localidadConsignatario = localidadConsignatario;
	}

	public String getProvinciaConsignatario() {
		return provinciaConsignatario;
	}

	public void setProvinciaConsignatario(String provinciaConsignatario) {
		this.provinciaConsignatario = provinciaConsignatario;
	}

	public String getCodigoPostalConsignatario() {
		return codigoPostalConsignatario;
	}

	public void setCodigoPostalConsignatario(String codigoPostalConsignatario) {
		this.codigoPostalConsignatario = codigoPostalConsignatario;
	}

	public String getFaxConsignatario() {
		return faxConsignatario;
	}

	public void setFaxConsignatario(String faxConsignatario) {
		this.faxConsignatario = faxConsignatario;
	}

	@Override
	public String toString() {
		return "LD{" +
				"inicio='" + inicio + '\'' +
				", direccionConsignatario='" + direccionConsignatario + '\'' +
				", localidadConsignatario='" + localidadConsignatario + '\'' +
				", provinciaConsignatario='" + provinciaConsignatario + '\'' +
				", codigoPostalConsignatario='" + codigoPostalConsignatario + '\'' +
				", faxConsignatario='" + faxConsignatario + '\'' +
				'}';
	}

	public LD() {
		super();
	}
}
