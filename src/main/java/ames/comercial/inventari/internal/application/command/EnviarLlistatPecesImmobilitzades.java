package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.InventariException.ErrorEnviantCorreuLlistatImmobilitzades;
import ames.comercial.inventari.internal.application.query.ExportarPecesImmobilitzades;
import ames.comercial.inventari.internal.infraestructure.setup.AdresesLlistaImmobilitzades;
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
 * Envia el llistat de peces immobilitzades d'un mes com a full de càlcul adjunt, a les adreces
 * configurades a {@code inventari.setup.adreses_llista_immobilitzades} (separades per coma).
 */
@Service
public class EnviarLlistatPecesImmobilitzades {

    private static final Logger logger = LoggerFactory.getLogger(EnviarLlistatPecesImmobilitzades.class);

    private static final DateTimeFormatter FORMAT_MES = DateTimeFormatter.ofPattern("MM/yyyy");

    private static final String EMAIL_FROM = "no-reply.comercial@ames.group";

    private static final String ASSUMPTE = "Llistat de peces immobilitzades {mes}";

    private static final String CONTINGUT = """
            Adjunt trobareu el llistat de peces immobilitzades de {mes}: articles-client amb
            existències al final del mes que ja en tenien 12 mesos abans i que no han tingut cap
            sortida durant aquest període.

            Aquest correu s'envia automàticament; no cal respondre'l.
            """;

    @Autowired AdresesLlistaImmobilitzades adresesLlistat;
    @Autowired ExportarPecesImmobilitzades exportarPecesImmobilitzades;
    @Autowired EmailValidation emailValidation;
    @Autowired IEmailAdresesProvider adresesProvider;
    @Autowired JavaMailSender mailSender;

    public void executar(YearMonth mes) {

        // Comprovació de les adreces d'email
        var listTo = emailValidation.splitAndCheck(adresesLlistat.obtenir());
        if (listTo.isEmpty()) {
            logger.warn("No hi ha cap adreça a inventari.setup.adreses_llista_immobilitzades: "
                    + "no s'envia el llistat de peces immobilitzades de {}", mes);
            return;
        }

        // Creació del missatge
        try {
            var fullCalcul = exportarPecesImmobilitzades.exportar(mes);
            MimeMessage missatge = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(missatge, true);
            // From
            helper.setFrom(EMAIL_FROM);
            // To
            helper.setTo(adresesProvider.provide(listTo));
            // Subject
            helper.setSubject(ASSUMPTE.replace("{mes}", mes.format(FORMAT_MES)));
            // Body
            helper.setText(CONTINGUT.replace("{mes}", mes.format(FORMAT_MES)));
            // Adjunt amb el llistat
            helper.addAttachment(String.format("immobilitzats_%s.xlsx", mes), new ByteArrayResource(fullCalcul));

            mailSender.send(missatge);
        } catch (Exception err) {
            throw new ErrorEnviantCorreuLlistatImmobilitzades(err);
        }
    }

}
