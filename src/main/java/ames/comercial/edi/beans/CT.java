package ames.comercial.edi.beans;

public class CT {

	public static final int i_inicio = 2;
	public static final int i_textoLibre = 37;

	private String inicio;
	private String textoLibre;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getTextoLibre() {
		return textoLibre;
	}

	public void setTextoLibre(String textoLibre) {
		this.textoLibre = textoLibre;
	}

	@Override
	public String toString() {
		return "CT [inicio=" + inicio + ", textoLibre=" + textoLibre + "]";
	}

	public CT() {
	}

}
