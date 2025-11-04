package ch.bzz.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
//                                "null", // e.g. file://...
//                                "http://localhost:8080",
//                                "https://localhost:8443",
//                                "https://*.github.io"
                                "https://alexanderpeter.github.io"
                        )
                        .allowedMethods("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return server -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(8080); // HTTP
            connector.setSecure(false);
            connector.setRedirectPort(8443); // HTTPS
            server.addAdditionalTomcatConnectors(connector);
        };
    }
}
