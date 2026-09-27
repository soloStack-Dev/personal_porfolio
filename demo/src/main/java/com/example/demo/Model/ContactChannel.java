package com.example.demo.Model;

/**
 * A direct-contact row in 04 / DIALOGUE.
 *
 * @param tone {@code purple} or {@code orange} icon accent
 * @param href optional; {@code null} renders plain text instead of a link
 */
public record ContactChannel(String icon, String label, String value, String tone, String href) {

    public static ContactChannel link(String icon, String label, String value, String tone, String href) {
        return new ContactChannel(icon, label, value, tone, href);
    }
}
