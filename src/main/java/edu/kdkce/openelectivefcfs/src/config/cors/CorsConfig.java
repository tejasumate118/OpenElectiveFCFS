package edu.kdkce.openelectivefcfs.src.config.cors;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("https://kdkce.openelective.click","https://*.lite.vusercontent.net", "https://*.ngrok-free.app"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "ngrok-skip-browser-warning" // Add this header
        ));
        config.setAllowedMethods(List.of("OPTIONS", "GET", "POST", "PUT", "DELETE", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}