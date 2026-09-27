package com.example.demo.Controller;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Service.ContactMailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
 *   <li>htmx request ({@code HX-Request: true}) → swap in a small HTML fragment, no page reload.
 *   <li>Plain browser POST (no JavaScript at all) → {@code Post/Redirect/Get} back to {@code /#contact}
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
                    new ContactDelivery(ContactDelivery.Status.SENT, "Thanks — your message is on its way."));
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
        redirectAttributes.addFlashAttribute("contactDelivery", delivery);
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
        // Flash the bound object back so the user does not have to retype a rejected message.
        redirectAttributes.addFlashAttribute("contactErrors", binding);
        redirectAttributes.addFlashAttribute("contactForm", binding.getTarget());
        if (delivery != null) {
            redirectAttributes.addFlashAttribute("contactDelivery", delivery);
        }
        return "redirect:/#contact";
    }
}
