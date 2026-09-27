package com.example.demo.Service;

import com.example.demo.Model.ContactChannel;
import com.example.demo.Model.ExpertiseCard;
import com.example.demo.Model.FilterOption;
import com.example.demo.Model.InfoColumn;
import com.example.demo.Model.Project;
import com.example.demo.Model.ProjectLink;
import com.example.demo.Model.Stat;
import com.example.demo.Model.TopicOption;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Single source of truth for every piece of copy rendered on the page.
 *
 * <p>Content mirrors {@code demo/Context/*.md} exactly. Keeping it here (rather than inline in
 * the templates) means the specs stay the spec and the view stays dumb.
 */
@Service
public class PortfolioService {

    public static final String OWNER_NAME = "Faleel H";
    public static final String OWNER_ROLE = "Java Developer";
    public static final String OWNER_EMAIL = "faleelmr4@gmail.com";
    public static final String OWNER_LOCATION = "Thiruvarur, Tamil Nadu, India";
    public static final String OWNER_GITHUB = "https://github.com/soloStack-Dev";
    public static final String OWNER_LINKEDIN = "https://www.linkedin.com/in/faleel-h-b772a1416";

    public List<Stat> stats() {
        return List.of(
                new Stat("7.58", "/10", "CGPA", "BCA — Bharath College of Science & Management."),
                new Stat("2", "+", "Projects Built", "Academic RAG app and a data-entry platform."),
                new Stat("10", "+", "Technologies", "Java, Spring Boot, MySQL, HTMX and more."),
                new Stat("21", "", "Java Version", "Spring Boot, Maven, and Docker."));
    }

    public List<InfoColumn> infoColumns() {
        return List.of(
                new InfoColumn("code", "Backend",
                        "Java · Spring Boot · Spring Data JPA", "REST APIs · WebSocket"),
                new InfoColumn("spark", "AI & RAG",
                        "Spring AI · Gemini", "RAG · Embeddings · Vector Search"),
                new InfoColumn("layout", "Database & Tools",
                        "MySQL · Redis · Chroma", "Git · GitHub · Docker · Maven"));
    }

    public List<ExpertiseCard> expertise() {
        return List.of(
                new ExpertiseCard(
                        "code",
                        "Core Languages",
                        "Programming languages and database technologies used for application development.",
                        List.of("Java", "JavaScript", "TypeScript", "SQL"),
                        false),
                new ExpertiseCard(
                        "layout",
                        "Backend & Web",
                        "Building backend services, APIs, and interactive web applications.",
                        List.of("Spring Boot", "Spring AI", "Spring Data JPA", "REST APIs", "WebSocket",
                                "HTML5", "CSS3", "HTMX", "Bootstrap", "jQuery"),
                        false),
                new ExpertiseCard(
                        "cloud",
                        "Database & AI",
                        "Working with relational databases, vector search, embeddings, and AI-powered applications.",
                        List.of("MySQL", "Redis", "Chroma Vector Database", "Retrieval-Augmented Generation (RAG)",
                                "Generative AI", "Embeddings", "Vector Search", "LLM"),
                        false),
                new ExpertiseCard(
                        "terminal",
                        "Testing & Dev Tools",
                        "Tools used for development, testing, version control, and application deployment.",
                        List.of("JUnit", "Git", "GitHub", "Docker", "Maven"),
                        false),
                new ExpertiseCard(
                        "network",
                        "Core Concepts",
                        "Fundamental concepts applied while developing web and backend applications.",
                        List.of("Object-Oriented Programming (OOP)", "DBMS", "REST Architecture",
                                "CRUD Operations", "API Integration"),
                        true));
    }

    public List<FilterOption> projectFilters() {
        return List.of(
                new FilterOption("all", "All"),
                new FilterOption("ai", "AI / RAG"),
                new FilterOption("webapp", "Web Applications"));
    }

    public List<Project> projects() {
        return List.of(
                new Project(
                        "rag-enterprise",
                        "AI / RAG",
                        "Academic Project",
                        "academic",
                        "RAG Application Enterprise",
                        "A source-grounded AI knowledge workspace that allows users to upload PDF documents, "
                                + "index their content, and ask questions using retrieved document context.",
                        List.of("Java 21", "Spring Boot", "Spring AI", "Gemini", "Chroma Vector Database",
                                "MySQL", "Spring Data JPA", "Thymeleaf", "HTMX", "Bootstrap"),
                        List.of(
                                "PDF document upload and processing",
                                "Document chunking and embedding generation",
                                "Vector-based semantic search using Chroma",
                                "Gemini-powered question answering",
                                "Source and page references for retrieved content",
                                "Persistent chat sessions and messages",
                                "Document processing status tracking",
                                "Document and chat deletion",
                                "Spring Boot integration and vector-store testing"),
                        List.of(ProjectLink.repository("https://github.com/soloStack-Dev/Rag_webapp.git", "code")),
                        List.of("ai")),
                new Project(
                        "data-entry-platform",
                        "Backend / Web Application",
                        "Academic Project",
                        "academic",
                        "Data Entry Management Platform",
                        "A server-rendered data-entry application for managing equipment movement between users "
                                + "and warehouses using Spring Boot, Thymeleaf, HTMX, and MySQL.",
                        List.of("Java 21", "Spring Boot", "Spring Data JPA", "Thymeleaf", "HTMX", "Bootstrap",
                                "MySQL", "Docker", "Maven"),
                        List.of(
                                "Create, edit, search, and delete data entries",
                                "Server-side validation with Bean Validation",
                                "Searchable and paginated data collection",
                                "HTMX partial page updates without full-page reloads",
                                "Dashboard with entry statistics and recent records",
                                "Service and repository based application architecture",
                                "MySQL database integration",
                                "Docker Compose application and database setup",
                                "Spring Boot Actuator health checks",
                                "Integration testing with a real application context"),
                        List.of(ProjectLink.repository("https://github.com/soloStack-Dev/Data_entry_management.git", "code")),
                        List.of("webapp")));
    }

    public List<ContactChannel> contactChannels() {
        return List.of(
                ContactChannel.link("mail", "Email", OWNER_EMAIL, "purple", "mailto:" + OWNER_EMAIL),
                ContactChannel.link("pin", "Location", OWNER_LOCATION, "orange", null),
                ContactChannel.link("briefcase", "LinkedIn", "LinkedIn Profile", "purple", OWNER_LINKEDIN),
                ContactChannel.link("code", "GitHub", "GitHub Profile", "purple", OWNER_GITHUB));
    }

    public List<TopicOption> topics() {
        return TopicOption.defaults();
    }
}
