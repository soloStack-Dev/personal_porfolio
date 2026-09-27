package com.example.demo.Model;

import java.util.List;

/**
 * One project card in 03 / PORTFOLIO.
 *
 * @param statusTone drives the status dot colour: live / metric / teams / academic / installs
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
