package ames.comercial.edi.beans;

public class LT {
	public static final int i_inicio = 2;
	public static final int i_texto1 = 42;
	public static final int i_texto2 = 82;
	public static final int i_texto3 = 122;
	public static final int i_texto4 = 162;

	private String inicio;
	private String texto1;
	private String texto2;
	private String texto3;
	private String texto4;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getTexto1() {
		return texto1;
	}

	public void setTexto1(String texto1) {
		this.texto1 = texto1;
	}

	public String getTexto2() {
		return texto2;
	}

	public void setTexto2(String texto2) {
		this.texto2 = texto2;
	}

	public String getTexto3() {
		return texto3;
	}

	public void setTexto3(String texto3) {
		this.texto3 = texto3;
	}

	public String getTexto4() {
		return texto4;
	}

	public void setTexto4(String texto4) {
		this.texto4 = texto4;
	}

	@Override
	public String toString() {
		return "LT{" +
				"inicio='" + inicio + '\'' +
				", texto1='" + texto1 + '\'' +
				", texto2='" + texto2 + '\'' +
				", texto3='" + texto3 + '\'' +
				", texto4='" + texto4 + '\'' +
				'}';
	}

	public LT() {
		super();
	}
}
