package ames.permisos;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomLogFilter extends Filter<ILoggingEvent> {

    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    /**
     * @return true (pintar queries) quan no està en produccio o test
     */
    private boolean isPrintQueries() {
        return !("produccio".equalsIgnoreCase(activeProfile)
                || "test".equals(activeProfile));
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // En cas que s'hagin de
        if (isPrintQueries())
            return FilterReply.ACCEPT;

        String message = event.getFormattedMessage();
        // Definir las palabras a excluir
        if (message.toUpperCase().contains("SELECT") && message.toUpperCase().contains("FROM")) {
            return FilterReply.DENY;  // No mostrar el log
        }
        return FilterReply.ACCEPT;  // Aceptar el log
    }

}
