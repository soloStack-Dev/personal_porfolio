package com.example.demo.Model;

import java.util.List;

/**
 * One project card in 03 / PORTFOLIO.
 *
 * @param statusTone drives the status dot colour. It is concatenated into a `.status--*` class
 *                    name in the template, so it must match a rule in site.css:
 *                    live / metric / teams / academic / self / installs. An unknown value is not
 *                    an error, it just renders with no colour, so add the CSS rule alongside.
 * @param tags       the technology stack
 * @param features   the "Key Features" bullet list
 * @param filters    ids of the 03 / PORTFOLIO filter pills this card belongs to
 */
public record Project(
        String id,
        String category,
        String status,
        String statusTone,
        String title,
        String description,
        List<String> tags,
        List<String> features,
        List<ProjectLink> links,
        List<String> filters) {
}
