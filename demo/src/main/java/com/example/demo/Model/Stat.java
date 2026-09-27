package com.example.demo.Model;

/**
 * A headline statistic tile in 01 / FOUNDATION.
 *
 * @param value    the big purple number, e.g. {@code 3.2M+}
 * @param unit     optional suffix rendered smaller next to the value, e.g. {@code yrs}
 * @param label    short bold label
 * @param note     small muted description
 */
public record Stat(String value, String unit, String label, String note) {
}
