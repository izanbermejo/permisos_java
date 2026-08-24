package ames.comercial.advantage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "advantage")
public class AdvantageConfig {

    private String path;
    private String pathmag;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPathmag() {
        return pathmag;
    }

    public void setPathmag(String pathmag) {
        this.pathmag = pathmag;
    }

}
