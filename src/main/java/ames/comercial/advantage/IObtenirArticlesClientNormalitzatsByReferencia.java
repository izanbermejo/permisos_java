package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirArticlesClientNormalitzatsByReferencia.ArticleClientUnitatsEmbalatge;

import java.util.Map;
import java.util.Set;

public interface IObtenirArticlesClientNormalitzatsByReferencia {

    Map<String, ArticleClientUnitatsEmbalatge> query(Set<String> referencies);

}
