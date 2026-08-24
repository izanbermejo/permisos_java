package ames.comercial.ofs.service;

import ames.comercial.shared.Numbers;

import java.math.BigDecimal;

public class ObtenirFactorReduccioFabricarGermans {

    /**
     * Funció que calcula el factor de reducció del lot òptim en cas que en la mateixa
     * OF s'hagin de fabricar peces de la mateixa familia (mateixa matriu -> els mateixos 4 primers codis de fàbrica)
     * Es feia servir la següent formula i es decideix canviar per aquesta taula que és l'equivalent
     * a aquesta funció ja que dificilment es passa de 2 germans per OF i la funció acaba
     * sent constant a partir del 12 (0.59)
     *
     * lot_optim * (((1 + (0.3 * (quantitat_germans - 1))) / quantitat_germans) ^ 1/2)
     * ==
     * arrel_quadrada(lot_optim * (((1 + (0.3 * (quantitat_germans - 1))) / quantitat_germans))
     *
     * @param quantitatGermans Quantitat de germans a fabricar en la OF
     * @return Factor de reducció
     */
    public BigDecimal executar (long quantitatGermans) {
        return switch ((int) quantitatGermans) {
            case 0, 1: yield Numbers.decimal("1");
            case 2 : yield Numbers.decimal("0.806225");
            case 3 : yield Numbers.decimal("0.730296");
            case 4 : yield Numbers.decimal("0.689202");
            case 5 : yield Numbers.decimal("0.663324");
            case 6 : yield Numbers.decimal("0.645497");
            default: yield Numbers.decimal("0.632455");
        };

    }

}
