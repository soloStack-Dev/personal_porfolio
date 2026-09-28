package com.example.demo.Controller;

import com.example.demo.Model.ContactForm;
import com.example.demo.Model.FilterOption;
import com.example.demo.Model.Project;
import com.example.demo.Service.PortfolioService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Serves the single server-rendered page. Everything else on the page is a Thymeleaf fragment.
 */
@Controller
public class PageController {

    private final PortfolioService portfolio;

    public PageController(PortfolioService portfolio) {
        this.portfolio = portfolio;
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(name = "filter", required = false) String filter) {

        // The 03 / PORTFOLIO filter is a query parameter, not a client-side toggle. The set of
        // cards that ships in the response is already the filtered one, which is why the pills
        // work as plain links and why /?filter=ai is shareable and survives a reload.
        FilterOption activeFilter = portfolio.resolveFilter(filter);
        List<Project> projects = portfolio.projectsFor(activeFilter.id());

        model.addAttribute("stats", portfolio.stats());
        model.addAttribute("infoColumns", portfolio.infoColumns());
        model.addAttribute("expertise", portfolio.expertise());
        model.addAttribute("filters", portfolio.projectFilters());
        model.addAttribute("activeFilterId", activeFilter.id());
        model.addAttribute("activeFilterLabel", activeFilter.label());
        model.addAttribute("projects", projects);
        model.addAttribute("projectCount", projects.size());
        model.addAttribute("projectCountLabel", projects.size() == 1 ? "project" : "projects");
        model.addAttribute("channels", portfolio.contactChannels());
        model.addAttribute("topics", portfolio.topics());

        // Identity, used by the header, hero, footer, and 04 / DIALOGUE.
        model.addAttribute("ownerName", PortfolioService.OWNER_NAME);
        model.addAttribute("ownerRole", PortfolioService.OWNER_ROLE);
        model.addAttribute("ownerEmail", PortfolioService.OWNER_EMAIL);
        model.addAttribute("ownerGithub", PortfolioService.OWNER_GITHUB);
        model.addAttribute("ownerLinkedin", PortfolioService.OWNER_LINKEDIN);
        model.addAttribute("currentYear", java.time.Year.now().getValue());

        // Consumed by the 04 / DIALOGUE fragment. Flash attributes from a no-JavaScript form
        // submission are already in the model by the time this handler runs, so the result
        // region is rendered with exactly the markup the htmx path swaps in.
        if (!model.asMap().containsKey("contactForm")) {
            model.addAttribute("contactForm", new ContactForm());
        }
        model.addAttribute("contactState", resolveContactState(model));
        model.addAttribute("contactFieldErrors", firstFieldMessages(model.asMap().get("contactErrors")));
        return "index";
    }

    private String resolveContactState(Model model) {
        if (model.asMap().containsKey("contactErrors")) {
            return "error";
        }
        return model.asMap().containsKey("contactDelivery") ? "success" : null;
    }

    /**
     * Flattens a flashed {@link BindingResult} into {@code field name -> first message}.
     *
     * <p>Done here rather than in the template because {@code th:errors} deletes its host
     * element when a field is valid — and the 04 / DIALOGUE error slots must survive in the DOM
     * so that htmx has something to out-of-band swap into.
     */
    private Map<String, String> firstFieldMessages(Object flashed) {
        if (!(flashed instanceof BindingResult binding) || !binding.hasFieldErrors()) {
            return Map.of();
        }
        Map<String, String> messages = new LinkedHashMap<>();
        binding.getFieldErrors().forEach(error -> messages.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return messages;
    }
}
