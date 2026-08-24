package ames.comercial.entrades.internal.entry;

import ames.comercial.entrades.internal.application.ImportarFitxerEntrades;
import ames.comercial.entrades.internal.infraestructure.InboxEntrades;
import ames.comercial.entrades.internal.infraestructure.InboxEntrades.OrigenEntrada;
import ames.comercial.entrades.internal.infraestructure.InboxEntradesErrorJson;
import ames.comercial.server.exception.AppException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EntradesListener {

    static final Logger log = LogManager.getLogger(ImportarFitxerEntrades.class.getName());

    private @Autowired InboxEntrades inboxEntrades;
    private @Autowired InboxEntradesErrorJson inboxEntradesErrorJson;
    private @Autowired ObjectMapper objectMapper;

    @RabbitListener(queues = "${cua.entrades}")
    @Transactional
    public void onMessage(String textMissatge) throws JsonProcessingException {
        log.info("IMPORT_ENTRADES Rebut missatge d'entrada: {}", textMissatge);

        EntradesListenerMissatge missatge = null;
        try {
            missatge = objectMapper.readValue(textMissatge, EntradesListenerMissatge.class);
        } catch (Exception e) {
            log.error("IMPORT_ENTRADES Error en processar missatge d'entrada: {}", textMissatge, e);
            inboxEntradesErrorJson.save(textMissatge);
            return;
        }

        var fabrica = missatge.codiContable();
        var numEnv = missatge.numEnv();
        var clauEntrada = String.format("E%s%05d.BCN", fabrica.toUpperCase(), numEnv);
        // Cal revisar que no s'hagi processat aquesta entrada, en aquest cas es descarta
        if (inboxEntrades.exists(clauEntrada)) {
            log.error("IMPORT_ENTRADES Fitxer d'entrada repetit: {}. Es descarta el fitxer: {}", clauEntrada, missatge);
            throw new AppException("IMPORT_ENTRADES Fitxer d'entrada repetit: " + clauEntrada);
        }
        inboxEntrades.save(clauEntrada, objectMapper.writeValueAsString(missatge.json()), OrigenEntrada.JSON);
    }

    @JsonDeserialize(builder = EntradesListenerMissatgeImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface EntradesListenerMissatge {
        String codiContable();
        long numEnv();
        JsonNode json();
    }


}
