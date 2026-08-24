package ames.comercial.edi.beans;

public class ZZ {
	public static final int i_inicio = 2;
	public static final int i_numeroRegitros = 12;
	
	private String inicio;
	private String numeroRegistros;	

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getNumeroRegistros() {
		return numeroRegistros;
	}

	public void setNumeroRegistros(String numeroRegistros) {
		this.numeroRegistros = numeroRegistros;
	}

	public ZZ() {
		super();
	}
}
