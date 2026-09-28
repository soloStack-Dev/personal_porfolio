package com.example.demo.Controller;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Model.FilterOption;
import com.example.demo.Model.Project;
import com.example.demo.Service.PortfolioService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        //
        // Everything is rebuilt from flash values that are guaranteed to have survived the trip.
        // ContactForm, BindingResult and ContactDelivery are not Serializable, so ContactController
        // flashes Strings and a LinkedHashMap instead of the objects themselves and this handler
        // puts the objects back. The fragment binds the inputs with th:object="${contactForm}" and
        // th:field="*{name}", which throws "Neither BindingResult nor plain target object for bean
        // name 'contactForm'" when the bean is absent - a 500 on the contact section of the whole
        // page, taken by every visitor who ever submitted the form. Rebuilding unconditionally is
        // what makes th:field safe on the first visit, the redirect target, and every render after.
        model.addAttribute("contactForm", rebuildContactForm(model));
        model.addAttribute("contactDelivery", rebuildContactDelivery(model));
        model.addAttribute("contactState", resolveContactState(model));
        model.addAttribute("contactFieldErrors", rebuildFieldMessages(model));
        return "index";
    }

    /**
     * Restores the visitor's rejected input, or an empty form on a first visit.
     */
    private ContactForm rebuildContactForm(Model model) {
        Map<String, Object> flashed = model.asMap();
        if (flashed.get("contactFormName") == null) {
            return new ContactForm();
        }
        ContactForm form = new ContactForm();
        form.setName(asString(flashed.get("contactFormName")));
        form.setEmail(asString(flashed.get("contactFormEmail")));
        form.setTopic(asString(flashed.get("contactFormTopic")));
        form.setMessage(asString(flashed.get("contactFormMessage")));
        return form;
    }

    private ContactDelivery rebuildContactDelivery(Model model) {
        Map<String, Object> flashed = model.asMap();
        Object status = flashed.get("contactDeliveryStatus");
        if (status == null) {
            return null;
        }
        return new ContactDelivery(
                ContactDelivery.Status.valueOf(asString(status)),
                asString(flashed.get("contactDeliveryMessage")));
    }

    private String resolveContactState(Model model) {
        Map<String, Object> flashed = model.asMap();
        if (flashed.get("contactFailed") != null) {
            return "error";
        }
        return flashed.get("contactDeliveryStatus") != null ? "success" : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> rebuildFieldMessages(Model model) {
        Object flashed = model.asMap().get("contactFieldErrorMessages");
        if (flashed instanceof Map<?, ?> map) {
            Map<String, String> messages = new LinkedHashMap<>();
            map.forEach((field, message) -> messages.put(String.valueOf(field), asString(message)));
            return messages;
        }
        return Map.of();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
