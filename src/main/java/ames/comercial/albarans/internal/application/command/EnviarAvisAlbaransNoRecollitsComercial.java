package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransServitsNoRecollits.AlbaraNoRecollit;
import ames.comercial.albarans.internal.services.GenerarTaulaAlbaransNoRecollits;
import ames.comercial.albarans.internal.services.GenerarTaulaAlbaransNoRecollits.Generador;
import ames.comercial.server.service.EmailValidation;
import ames.comercial.server.service.IEmailAdresesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Envia a cada persona el recull dels albarans que ha creat que el magatzem ja ha servit però que el
 * transportista encara no ha recollit.
 * <p>
 * S'agrupa per l'empleat que va crear l'albarà i s'envia un correu a la seva adreça de
 * {@code cache.cache_empleats}. Els albarans dels quals no se'n pot deduir cap adreça (els creats per
 * processos automàtics, que no tenen usuari de creació, i els dels empleats que no en tenen informada)
 * queden fora d'aquest avís, però continuen sortint al del magatzem
 * ({@link EnviarAvisAlbaransNoRecollitsMagatzem}), que és qui hi ha d'actuar.
 * <p>
 * Un error enviant a una persona (típicament una adreça mal formada a la cache) es registra i no atura
 * la resta dels enviaments: l'avís és diari i no val la pena perdre'l tot per un destinatari.
 */
@Service
public class EnviarAvisAlbaransNoRecollitsComercial {

    private static final Logger logger = LoggerFactory.getLogger(EnviarAvisAlbaransNoRecollitsComercial.class);

    private static final String EMAIL_FROM = "no-reply.comercial@ames.group";

    private static final String ASSUMPTE = "Revisa la llista adjunta d'albarans no recollits / "
            + "Check the attached list of uncollected delivery notes";

    private static final String INTRODUCCIO_CATALA = "Aquests són els albarans que has creat que el magatzem "
            + "ja ha servit i que el transportista encara no ha recollit.";

    private static final String INTRODUCCIO_ANGLES = "These are the delivery notes you created that the "
            + "warehouse has already dispatched and that the carrier has not collected yet.";

    @Autowired GenerarTaulaAlbaransNoRecollits generarTaula;
    @Autowired EmailValidation emailValidation;
    @Autowired IEmailAdresesProvider adresesProvider;
    @Autowired JavaMailSender mailSender;

    public void executar(List<AlbaraNoRecollit> albarans) {

        var albaransSenseEmail = albarans.stream().filter(a -> a.emailCreacio().isEmpty()).count();
        if (albaransSenseEmail > 0) {
            logger.warn("{} albarans servits i no recollits no tenen cap adreça de l'usuari de creació: "
                    + "només sortiran a l'avís del magatzem", albaransSenseEmail);
        }

        // Només s'agrupen els albarans dels quals se'n pot deduir una adreça: la resta no es poden avisar
        var albaransPerEmpleat = albarans.stream()
                .filter(albara -> albara.emailCreacio().isPresent())
                .collect(groupingBy(AlbaraNoRecollit::usufabCreacio, LinkedHashMap::new, toList()));
        if (albaransPerEmpleat.isEmpty()) {
            logger.info("No hi ha cap albarà servit i no recollit per avisar cap persona");
            return;
        }

        var generador = generarTaula.preparar();
        for (var albaransEmpleat : albaransPerEmpleat.values()) {
            enviar(generador, albaransEmpleat);
        }
    }

    /** Tots els albarans de la llista són del mateix empleat, de manera que el destinatari surt del primer. */
    private void enviar(Generador generador, List<AlbaraNoRecollit> albarans) {
        var empleat = albarans.get(0);
        try {
            var listTo = emailValidation.splitAndCheck(empleat.emailCreacio().orElseThrow());

            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true, "UTF-8");
            // From
            helper.setFrom(EMAIL_FROM);
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // Subject
            helper.setSubject(ASSUMPTE);
            // Body (en HTML: el recull és una taula)
            helper.setText(generador.generarCorreu(INTRODUCCIO_CATALA, INTRODUCCIO_ANGLES, albarans, false), true);

            mailSender.send(missatge);
            logger.info("ENVIAT l'avís de {} albarans servits i no recollits a {} ({})",
                    albarans.size(), empleat.nomCreacio(), empleat.usufabCreacio());
        } catch (Exception err) {
            logger.error(String.format("ERROR enviant l'avís d'albarans servits i no recollits a %s (%d)",
                    empleat.nomCreacio(), empleat.usufabCreacio()), err);
        }
    }

}
