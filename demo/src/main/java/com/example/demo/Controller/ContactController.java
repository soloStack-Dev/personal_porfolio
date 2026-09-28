package com.example.demo.Controller;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Service.ContactMailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Receives the 04 / DIALOGUE form and is built as a proper progressive enhancement:
 *
 * <ul>
 *   <li>htmx request ({@code HX-Request: true}) â†’ swap in a small HTML fragment, no page reload.
 *   <li>Plain browser POST (no JavaScript at all) â†’ {@code Post/Redirect/Get} back to {@code /#contact}
 *       with the outcome carried in a flash attribute.
 * </ul>
 *
 * <p>Both paths produce the same DOM, because the htmx fragment and the server-rendered form use
 * the identical markup and the same {@code id}s for the per-field error slots.
 */
@Controller
public class ContactController {

    private static final String HX_REQUEST = "HX-Request";

    private final ContactMailService mailService;

    public ContactController(ContactMailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/contact")
    public String submit(
            @Valid @ModelAttribute("contactForm") ContactForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        boolean htmx = "true".equalsIgnoreCase(request.getHeader(HX_REQUEST));

        // Honeypot: answer exactly as if it worked, so bots learn nothing.
        if (form.getWebsite() != null && !form.getWebsite().isBlank()) {
            return succeed(htmx, model, redirectAttributes,
                    new ContactDelivery(ContactDelivery.Status.SENT, "Thanks â€” your message is on its way."));
        }

        if (binding.hasErrors()) {
            return fail(htmx, model, redirectAttributes, binding, null);
        }

        ContactDelivery delivery = mailService.submit(form);
        if (!delivery.delivered()) {
            return fail(htmx, model, redirectAttributes, binding, delivery);
        }
        return succeed(htmx, model, redirectAttributes, delivery);
    }

    private String succeed(
            boolean htmx, Model model, RedirectAttributes redirectAttributes, ContactDelivery delivery) {
        if (htmx) {
            model.addAttribute("state", "success");
            model.addAttribute("delivery", delivery);
            return "fragments/contact-result :: outcome";
        }
        redirectAttributes.addFlashAttribute("contactDeliveryStatus", delivery.status().name());
        redirectAttributes.addFlashAttribute("contactDeliveryMessage", delivery.detail());
        return "redirect:/#contact";
    }

    private String fail(
            boolean htmx,
            Model model,
            RedirectAttributes redirectAttributes,
            BindingResult binding,
            ContactDelivery delivery) {
        if (htmx) {
            model.addAttribute("state", "error");
            model.addAttribute("errors", binding);
            model.addAttribute("delivery", delivery);
            return "fragments/contact-result :: outcome";
        }
        // Flashed attributes are round-tripped through the session, and a session does not have to
        // serialize - but if it ever does, only genuinely serializable values survive. ContactForm,
        // BindingResult and ContactDelivery are not Serializable, so flashing them directly leaves
        // the redirect target at the mercy of the container. When one did not come back,
        // th:object="${contactForm}" had nothing to bind to and the redirect target threw
        // "Neither BindingResult nor plain target object for bean name 'contactForm'", 500ing the
        // whole page for every visitor who ever submitted the form. Strings and LinkedHashMap are
        // serializable, so the values travel as those and PageController rebuilds the objects.
        ContactForm form = (ContactForm) binding.getTarget();
        redirectAttributes.addFlashAttribute("contactFailed", Boolean.TRUE);
        redirectAttributes.addFlashAttribute("contactFormName", form.getName());
        redirectAttributes.addFlashAttribute("contactFormEmail", form.getEmail());
        redirectAttributes.addFlashAttribute("contactFormTopic", form.getTopic());
        redirectAttributes.addFlashAttribute("contactFormMessage", form.getMessage());
        redirectAttributes.addFlashAttribute("contactFieldErrorMessages", firstFieldMessages(binding));
        if (delivery != null) {
            redirectAttributes.addFlashAttribute("contactDeliveryStatus", delivery.status().name());
            redirectAttributes.addFlashAttribute("contactDeliveryMessage", delivery.detail());
        }
        return "redirect:/#contact";
    }

    /**
     * Flattens a {@link BindingResult} into a serializable {@code field name -> first message}.
     *
     * <p>Done here rather than in the template because {@code th:errors} deletes its host element
     * when a field is valid, and the 04 / DIALOGUE error slots must survive in the DOM so that htmx
     * has something to out-of-band swap into.
     */
    private static Map<String, String> firstFieldMessages(BindingResult binding) {
        if (!binding.hasFieldErrors()) {
            return new LinkedHashMap<>();
        }
        Map<String, String> messages = new LinkedHashMap<>();
        binding.getFieldErrors().forEach(error -> messages.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return messages;
    }
}
