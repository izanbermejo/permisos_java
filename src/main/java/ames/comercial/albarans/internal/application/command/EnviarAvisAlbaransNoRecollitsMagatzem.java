package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.IObtenirEmailResponsableMagatzemAds;
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
 * Envia a cada magatzem el recull dels seus albarans que ja ha servit però que el transportista encara
 * no ha recollit.
 * <p>
 * A diferència de l'avís que rep cada persona ({@link EnviarAvisAlbaransNoRecollitsComercial}), el del
 * magatzem porta també el nom i els cognoms de qui va crear cada albarà, perquè el magatzem sàpiga amb
 * qui ha de parlar. S'envia un correu per magatzem a la seva adreça de l'Advantage
 * ({@code magdb.magatzems.emailmag}), de manera que cada magatzem només veu els seus albarans.
 * <p>
 * Un error enviant a un magatzem es registra i no atura la resta dels enviaments.
 */
@Service
public class EnviarAvisAlbaransNoRecollitsMagatzem {

    private static final Logger logger = LoggerFactory.getLogger(EnviarAvisAlbaransNoRecollitsMagatzem.class);

    private static final String EMAIL_FROM = "no-reply.comercial@ames.group";

    private static final String ASSUMPTE = "Revisa la llista adjunta d'albarans no recollits / "
            + "Check the attached list of uncollected delivery notes";

    private static final String INTRODUCCIO_CATALA = "Aquests són els albarans del magatzem {magatzem} que ja "
            + "s'han servit i que el transportista encara no ha recollit.";

    private static final String INTRODUCCIO_ANGLES = "These are the delivery notes of warehouse {magatzem} "
            + "that have already been dispatched and that the carrier has not collected yet.";

    @Autowired IObtenirEmailResponsableMagatzemAds obtenirEmailMagatzem;
    @Autowired GenerarTaulaAlbaransNoRecollits generarTaula;
    @Autowired EmailValidation emailValidation;
    @Autowired IEmailAdresesProvider adresesProvider;
    @Autowired JavaMailSender mailSender;

    public void executar(List<AlbaraNoRecollit> albarans) {

        var albaransPerMagatzem = albarans.stream()
                .collect(groupingBy(AlbaraNoRecollit::magatzem, LinkedHashMap::new, toList()));
        if (albaransPerMagatzem.isEmpty()) {
            logger.info("No hi ha cap albarà servit i no recollit per avisar cap magatzem");
            return;
        }

        var generador = generarTaula.preparar();
        albaransPerMagatzem.forEach((magatzem, albaransMagatzem) -> enviar(generador, magatzem, albaransMagatzem));
    }

    private void enviar(Generador generador, String magatzem, List<AlbaraNoRecollit> albarans) {
        try {
            var email = obtenirEmailMagatzem.query(magatzem).filter(adresa -> !adresa.isBlank());
            if (email.isEmpty()) {
                logger.warn("El magatzem {} no té cap adreça a l'Advantage: no s'hi envia l'avís de {} albarans "
                        + "servits i no recollits", magatzem, albarans.size());
                return;
            }
            var listTo = emailValidation.splitAndCheck(email.get());

            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true, "UTF-8");
            // From
            helper.setFrom(EMAIL_FROM);
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // Subject
            helper.setSubject(ASSUMPTE);
            // Body (en HTML: el recull és una taula)
            helper.setText(generador.generarCorreu(
                    INTRODUCCIO_CATALA.replace("{magatzem}", magatzem),
                    INTRODUCCIO_ANGLES.replace("{magatzem}", magatzem),
                    albarans, true), true);

            mailSender.send(missatge);
            logger.info("ENVIAT l'avís de {} albarans servits i no recollits al magatzem {}",
                    albarans.size(), magatzem);
        } catch (Exception err) {
            logger.error(String.format("ERROR enviant l'avís d'albarans servits i no recollits al magatzem %s",
                    magatzem), err);
        }
    }

}
