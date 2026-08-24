package ames.comercial.albarans.internal.services;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Calcula, per a una línia a servir, l'avís d'embalatge que correspon quan la quantitat no és
 * múltiple de les unitats d'embalatge de l'article-client.
 * <p>
 * Rèplica fidel de la funció {@code TestQEmb} del legacy {@code reportlegacy/mantalb.pas}
 * (línies ~12019-12061), basada en {@code ACLUCAI} (unitats per caixa) i {@code ACLUCAP}
 * (caixes per palet). El comportament estricte del legacy sobre el magatzem propi {@code MAGCOD='0001'}
 * es generalitza a tots els magatzems controlats internament ({@code mag.tipus='N'}).
 * <p>
 * És un servei sense estat i pur: no accedeix a cap recurs i no llença excepcions. Serveix tant per a
 * la proposta d'albarans de sortida com de traspàs.
 */
@Service
public class CalcularAvisosEmbalatge {

    /**
     * @param quantitat                  quantitat a servir de la línia
     * @param unitatsEmbalatge           unitats per caixa de l'article-client ({@code ACLUCAI})
     * @param caixesPalet                caixes per palet de l'article-client ({@code ACLUCAP}); 0 si no n'hi ha
     * @param origenControlatInternament cert si el magatzem d'origen és controlat internament ({@code mag.tipus='N'}), és a dir no es poden obrir caixes
     * @return l'avís corresponent, o buit si la quantitat és correcta
     */
    public Optional<AvisEmbalatge> calcular(long quantitat, long unitatsEmbalatge, long caixesPalet,
                                            boolean origenControlatInternament) {
        // Sense unitats per caixa definides no es pot validar: cal revisar l'article-client
        if (unitatsEmbalatge == 0) {
            return Optional.of(AvisEmbalatge.SENSE_EMBALATGE_DEFINIT);
        }

        // Amb caixes per palet definides, es controla el múltiple de tot el condicionament (unitats/caixa × caixes/palet)
        if (caixesPalet != 0) {
            if (quantitat % (unitatsEmbalatge * caixesPalet) != 0) {
                return Optional.of(AvisEmbalatge.NO_MULTIPLE_CONDICIONAMENT);
            }
            return Optional.empty();
        }

        // Sense caixes per palet, es controla el múltiple de les unitats per caixa
        if (quantitat % unitatsEmbalatge != 0) {
            // En magatzem controlat internament no es poden obrir caixes (avís); en plataforma és només informatiu
            return Optional.of(origenControlatInternament
                    ? AvisEmbalatge.NO_MULTIPLE_CAIXA
                    : AvisEmbalatge.NO_MULTIPLE_CAIXA_PLATAFORMA);
        }

        return Optional.empty();
    }

}
