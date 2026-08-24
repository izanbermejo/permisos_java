package ames.comercial.entrades.events;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class EntradaArticleEvent extends ApplicationEvent {

	KeyArticleClient articleClient;
	Empresa empresa;

	public EntradaArticleEvent(Object source, KeyArticleClient articleClient, Empresa empresa) {
		super(source);
		this.articleClient = articleClient;
		this.empresa = empresa;
	}

	public KeyArticleClient articleClient() {
		return this.articleClient;
	}

	public Empresa empresa() {
		return this.empresa;
	}
	
}