package ames.permisos;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableTransactionManagement
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
			.bannerMode(Banner.Mode.OFF)
    		.run(args);
	}
	
	@Bean
	public ResourceConfig jerseyConfiguration() {
		// Deshabilitat WADL
		Map<String, Object> properties = new HashMap<>();
		properties.put(ServerProperties.WADL_FEATURE_DISABLE, true);
		return new ResourceConfig(JacksonJaxbJsonProvider.class)
				.packages("ames.permisos")
				.addProperties(properties)
				.register(MultiPartFeature.class);
	}
	
	/** Configuració de Jackson per la serialització i deserialització amb JSON */
	@Bean
	@Primary
	public ObjectMapper jsonMapper() {
		return JsonMapper.builder()
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)				// Accepta propietats en JSON amb majúscules o minúscules
			.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)		// Serialització sense nanosegons
			.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)	// Deserialització sense nanosegons
		 	.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)				// No es llença un error si en la deserialització no es troben tots els camps
		 	.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)				// java.util.Date en format ISO-8601
		 	.serializationInclusion(Include.NON_NULL)							// En la serialització a json no s'inclouen els camps null
		 	.addModule(new Jdk8Module())
			.addModule(new JavaTimeModule())
			.build();
	}
	
	@Primary
	@Bean(name = "amesDataSource")
	@ConfigurationProperties(prefix = "ames.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	
	
	@Primary
	@Bean(name = "jdbcAmes")
	public JdbcTemplate jdbc(@Qualifier("amesDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	@Primary
	@Bean(name = "jdbcNamingAmes")
	public NamedParameterJdbcTemplate naming(@Qualifier("amesDataSource") DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

}
