package com.example.demo.Model;

import java.util.List;

/**
 * One expertise card in 02 / CAPABILITIES.
 *
 * @param wide        spans two grid columns on desktop, collapses to one on mobile
 */
public record ExpertiseCard(
        String icon,
        String title,
        String description,
        List<String> tags,
        boolean wide) {
}
