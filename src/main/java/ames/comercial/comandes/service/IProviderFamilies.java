package ames.comercial.comandes.service;

import ames.comercial.shared.KeyArticleClient;

import java.util.Set;

public interface IProviderFamilies {

	ProviderFamilies.IProviderFamiliesResponse provide(Set<KeyArticleClient> articles);
	
}
