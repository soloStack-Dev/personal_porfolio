package com.example.demo.Model;

/**
 * A link inside a project card, e.g. "Live Demo" / "Source Repository".
 *
 * <p>{@code href} is nullable on purpose. A link without one is not rendered as a dead
 * {@code <a href="#">}, and it is not a button that needs a click to admit it: the server
 * already knows there is no URL, so the template renders it as a non-interactive element that
 * says so. No JavaScript involved in telling the visitor the truth.
 *
 * @param tone {@code primary} (purple) or {@code secondary} (slate)
 */
public record ProjectLink(String label, String icon, String tone, String href) {

    /** True when this link has somewhere to actually go. */
    public boolean isAvailable() {
        return href != null && !href.isBlank();
    }

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
