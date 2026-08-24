package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirFifoPeses.FifoPeses;
import ames.comercial.shared.KeyArticleClient;

import java.util.List;

public interface IObtenirFifoPeses {
    List<FifoPeses> get(KeyArticleClient articleClient);
}
