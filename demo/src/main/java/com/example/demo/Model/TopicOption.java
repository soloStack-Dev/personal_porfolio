package com.example.demo.Model;

import java.util.List;

/** A topic of interest offered by the 04 / DIALOGUE dropdown. */
public record TopicOption(String value, String label) {

    public static List<TopicOption> defaults() {
        return List.of(
                new TopicOption("Job Opportunity", "Job Opportunity"),
                new TopicOption("Java Development", "Java Development"),
                new TopicOption("Project Discussion", "Project Discussion"),
                new TopicOption("Collaboration", "Collaboration"),
                new TopicOption("Technical Discussion", "Technical Discussion"),
                new TopicOption("Something Else", "Something Else"));
    }
}
