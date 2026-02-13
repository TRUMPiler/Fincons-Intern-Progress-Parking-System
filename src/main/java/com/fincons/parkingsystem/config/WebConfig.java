package com.fincons.parkingsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for web-related settings, including Cross-Origin Resource Sharing (CORS).
 * This class implements the WebMvcConfigurer interface to customize the Spring MVC configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer{
    /**
     * Configures CORS mappings for the application.
     * This method defines the rules for which origins, methods, and headers are allowed
     * when making cross-origin requests to the API.
     *
     * @param registry the CorsRegistry to which the CORS configuration is added.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Applies CORS configuration to all endpoints under /api/
                .allowedOrigins("http://localhost:4200","https://www.finconsparkingsystem.live","https://finconsparkingsystem.live","https://api.finconsparkingsystem.live") // Allows requests from the specified origin
                .allowedMethods("*") // Allows all HTTP methods (GET, POST, PUT, DELETE, etc.)
                .allowedHeaders("*") // Allows all headers in the request
                .allowCredentials(true); // Allows credentials (e.g., cookies, authorization headers)
    }
}
