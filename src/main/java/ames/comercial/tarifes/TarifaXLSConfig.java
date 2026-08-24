package ames.comercial.tarifes;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "comercial.tarifes.xls")
public class TarifaXLSConfig {

    private String numofcolumns;

    public String getNumofcolumns() {
        return numofcolumns;
    }

    public void setNumofcolumns(String numofcolumns) {
        this.numofcolumns = numofcolumns;
    }
}
