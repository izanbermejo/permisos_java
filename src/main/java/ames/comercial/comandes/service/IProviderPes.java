package ames.comercial.comandes.service;

import ames.comercial.comandes.service.ProviderPes.IProviderPesResponse;

import java.util.Set;

public interface IProviderPes {

	IProviderPesResponse provide(String artint);
	IProviderPesResponse provide(Set<String> articles);
	
}
