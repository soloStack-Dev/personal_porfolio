package com.example.demo.Model;

/**
 * A link inside a project card, e.g. "Live Demo" / "Source Repository".
 *
 * <p>{@code href} is nullable on purpose: a link without one is rendered as a
 * {@code data-placeholder-link} button that explains it is not wired up yet, instead of a
 * dead {@code <a>}.
 *
 * @param tone {@code primary} (purple) or {@code secondary} (slate)
 */
public record ProjectLink(String label, String icon, String tone, String href) {

    public static ProjectLink primary(String label, String icon) {
        return new ProjectLink(label, icon, "primary", null);
    }

    public static ProjectLink secondary(String label, String icon) {
        return new ProjectLink(label, icon, "secondary", null);
    }

    /** A real outbound link, rendered as an {@code <a target="_blank" rel="noopener">}. */
    public static ProjectLink external(String label, String icon, String href) {
        return new ProjectLink(label, icon, "primary", href);
    }

    /** The Source Repository action shared by both project cards. */
    public static ProjectLink repository(String href, String icon) {
        return new ProjectLink("Source Repository", icon, "secondary", href);
    }
}
