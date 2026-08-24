package ames.comercial.edi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ComandaEDIConfig {

    @Value("${docsapps-folder}") private String rootDirectory;

    private final String APP_DIRECTORY = "/comercial/edis_indra";

    public String getDirIN() {
        return rootDirectory+APP_DIRECTORY+"/in";
    }

    public String getDirBKP() {
        return rootDirectory+APP_DIRECTORY+"/bkp";
    }

    public String getDirERROR() {
        return rootDirectory+APP_DIRECTORY+"/err";
    }

    public String getDirPDF() { return rootDirectory+APP_DIRECTORY+"/pdf"; }

    public static String DELIMITER = "ZZ";

}
