package ames.comercial.edi.beans;

public class LL {

	public static final int i_inicio = 2;
	public static final int i_codigoEtiqueta = 19;
	public static final int i_texto = 54;

	private String inicio;
	private String codigoEtiqueta;
	private String texto;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getCodigoEtiqueta() {
		return codigoEtiqueta;
	}

	public void setCodigoEtiqueta(String codigoEtiqueta) {
		this.codigoEtiqueta = codigoEtiqueta;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	@Override
	public String toString() {
		return "LL{" +
				"inicio='" + inicio + '\'' +
				", codigoEtiqueta='" + codigoEtiqueta + '\'' +
				", texto='" + texto + '\'' +
				'}';
	}

	public LL() {
		super();
	}
}
