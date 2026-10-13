package com.microshop.users.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(new Locale("es", "PE")); // Español peruano
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Dos juegos de mensajes (i18n/ y la raíz de resources): sólo se leía el primero, así que 13 claves en
        // uso (p. ej. auth.credentials.invalid) lanzaban NoSuchMessageException y un PIN o login erróneo daba 500.
        messageSource.setBasenames("classpath:i18n/messages", "classpath:messages");
        // Otras 35 claves en uso (sobre todo de RRHH) no están en ningún archivo: mejor el código como texto y el
        // 4xx correcto que un 500. Pendiente redactarlas (ver PENDIENTES).
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setCacheSeconds(3600); // Reload messages every hour
        return messageSource;
    }
}
