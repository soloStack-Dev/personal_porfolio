package com.example.demo.Model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Destination + sender for the contact form. Bound from {@code app.contact.*}.
 * Defaults keep the site working with zero configuration.
 */
@ConfigurationProperties(prefix = "app.contact")
public record ContactProperties(
        @DefaultValue("faleelmr4@gmail.com") String to,
        @DefaultValue("portfolio@localhost") String from,
        @DefaultValue("New portfolio message") String subjectPrefix) {
}
