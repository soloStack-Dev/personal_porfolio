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
        @DefaultValue(PLACEHOLDER_SENDER) String from,
        @DefaultValue("New portfolio message") String subjectPrefix) {

    /**
     * Default {@code from}. Not a real mailbox: a relay rejects it with
     * {@code 550 The specified from address does not match a permitted sender}, which is a
     * clearer signal than silently sending as nobody, and {@code ContactMailService} warns once
     * if it is still in place while a relay is configured.
     */
    public static final String PLACEHOLDER_SENDER = "portfolio@localhost";
}
