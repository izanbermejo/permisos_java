package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.InventariException.ErrorEnviantCorreuLlistatFerralla;
import ames.comercial.inventari.internal.application.query.ExportarFerralla;
import ames.comercial.inventari.internal.application.query.ObtenirFerralla;
import ames.comercial.inventari.internal.infraestructure.setup.AdresesLlistaFerralla;
import ames.comercial.server.service.EmailValidation;
import ames.comercial.server.service.IEmailAdresesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Envia el llistat de ferralla com a full de càlcul adjunt, a les adreces configurades a
 * {@code inventari.setup.adreses_llista_ferralla} (separades per coma).
 * <p>
 * L'assumpte, el text i el nom de l'adjunt s'identifiquen pel període que cobreix el llistat i no
 * pel mes de referència, que no hi entra: qui rep el correu ha de veure de quins mesos és la
 * ferralla.
 */
@Service
public class EnviarLlistatFerralla {

    private static final Logger logger = LoggerFactory.getLogger(EnviarLlistatFerralla.class);

    private static final DateTimeFormatter FORMAT_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private static final String EMAIL_FROM = "no-reply.comercial@ames.group";

    private static final String ASSUMPTE = "Llistat de ferralla de {mesInici} a {mesFi}";

    private static final String CONTINGUT = """
            Adjunt trobareu el llistat de ferralla de {mesInici} a {mesFi}: els moviments de ferralla
            d'aquests 12 mesos, acumulats per article-client i valorats a preu de cost en euros.

            Aquest correu s'envia automàticament; no cal respondre'l.
            """;

    @Autowired AdresesLlistaFerralla adresesLlistat;
    @Autowired ExportarFerralla exportarFerralla;
    @Autowired EmailValidation emailValidation;
    @Autowired IEmailAdresesProvider adresesProvider;
    @Autowired JavaMailSender mailSender;

    public void executar(YearMonth mes) {

        // Comprovació de les adreces d'email
        var listTo = emailValidation.splitAndCheck(adresesLlistat.obtenir());
        if (listTo.isEmpty()) {
            logger.warn("No hi ha cap adreça a inventari.setup.adreses_llista_ferralla: "
                    + "no s'envia el llistat de ferralla dels 12 mesos anteriors a {}", mes);
            return;
        }

        var periode = ObtenirFerralla.periodeDe(mes);
        var mesInici = YearMonth.from(periode.dataInici()).format(FORMAT_MES);
        var mesFi = YearMonth.from(periode.dataFi()).format(FORMAT_MES);

        // Creació del missatge
        try {
            var fullCalcul = exportarFerralla.exportar(mes);
            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true);
            // From
            helper.setFrom(EMAIL_FROM);
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // Subject
            helper.setSubject(ASSUMPTE.replace("{mesInici}", mesInici).replace("{mesFi}", mesFi));
            // Body
            helper.setText(CONTINGUT.replace("{mesInici}", mesInici).replace("{mesFi}", mesFi));
            // Adjunt amb el llistat
            helper.addAttachment(
                    String.format("ferralla_%s_%s.xlsx", YearMonth.from(periode.dataInici()),
                            YearMonth.from(periode.dataFi())),
                    new ByteArrayResource(fullCalcul));

            mailSender.send(missatge);
        } catch (Exception err) {
            throw new ErrorEnviantCorreuLlistatFerralla(err);
        }
    }

}
