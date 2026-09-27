package com.example.demo.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Backing bean for the 04 / DIALOGUE contact form.
 *
 * <p>A mutable JavaBean (rather than a record) so that {@code th:field} and
 * {@code BindingResult} re-populate the form after a failed submission.
 */
public class ContactForm {

    @NotBlank(message = "Please tell me your name.")
    @Size(max = 80, message = "Please keep your name under 80 characters.")
    private String name;

    @NotBlank(message = "An email address is required.")
    @Email(message = "That email address doesn't look quite right.")
    @Size(max = 160, message = "Please keep the email address under 160 characters.")
    private String email;

    @NotBlank(message = "Please choose a subject so I know where to route this.")
    private String topic;

    @NotBlank(message = "A short message helps me reply usefully.")
    @Size(min = 20, message = "Please write at least 20 characters.")
    @Size(max = 2000, message = "Please keep the message under 2000 characters.")
    private String message;

    /** Honeypot. Bots fill it, humans never see it. */
    private String website;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
