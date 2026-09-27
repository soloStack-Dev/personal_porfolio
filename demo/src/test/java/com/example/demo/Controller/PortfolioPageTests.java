package com.example.demo.Controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.demo.Model.ContactDelivery;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Covers the two things an agent will most likely break: the page rendering, and the contact
 * form's two submission paths (htmx fragment vs. plain Post/Redirect/Get).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PortfolioPageTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void indexRendersEveryNumberedSection() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(Matchers.containsString("01 / Foundation")))
                .andExpect(content().string(Matchers.containsString("02 / Capabilities")))
                .andExpect(content().string(Matchers.containsString("03 / Portfolio")))
                .andExpect(content().string(Matchers.containsString("04 / Dialogue")))
                .andExpect(content().string(Matchers.containsString("RAG Application Enterprise")))
                .andExpect(content().string(Matchers.containsString("Data Entry Management Platform")))
                .andExpect(content().string(Matchers.containsString("Core Concepts")));
    }

    @Test
    void theBrandAndOwnerDetailsAreTheRealOnes() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.containsString("Faleel H")))
                .andExpect(content().string(Matchers.containsString("Java Developer")))
                .andExpect(content().string(Matchers.containsString("BCA Graduate")))
                .andExpect(content().string(Matchers.containsString("faleelmr4@gmail.com")))
                .andExpect(content().string(Matchers.containsString("Thiruvarur, Tamil Nadu, India")))
                .andExpect(content().string(Matchers.containsString("7.58")))
                .andExpect(content().string(Matchers.containsString("Bharath College of Science and Management")));
    }

    @Test
    void noFictitiousIdentityIsLeftOnThePage() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Alex Rivera"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("example.dev"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("NexusCloud"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("99.99% Uptime"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("4A91 2E78 FC02 91BC"))));
    }

    @Test
    void theProjectRepositoriesAndSocialProfilesAreRealLinks() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.containsString(
                        "href=\"https://github.com/soloStack-Dev/Rag_webapp.git\"")))
                .andExpect(content().string(Matchers.containsString(
                        "href=\"https://github.com/soloStack-Dev/Data_entry_management.git\"")))
                .andExpect(content().string(Matchers.containsString(
                        "href=\"https://github.com/soloStack-Dev\"")))
                .andExpect(content().string(Matchers.containsString(
                        "href=\"https://www.linkedin.com/in/faleel-h-b772a1416\"")))
                .andExpect(content().string(Matchers.containsString("rel=\"noopener noreferrer\"")))
                // Nothing is left as a dead placeholder for the links we do have.
                .andExpect(content().string(Matchers.not(
                        Matchers.containsString("data-placeholder-link=\"Source Repository\""))));
    }

    @Test
    void indexExposesContentTheClientSideFilterNeeds() throws Exception {
        mvc.perform(get("/"))
                .andExpect(model().attributeExists("projects", "filters", "expertise", "stats", "channels"))
                .andExpect(content().string(Matchers.containsString("data-filters=\"ai\"")))
                .andExpect(content().string(Matchers.containsString("data-filters=\"webapp\"")))
                .andExpect(content().string(Matchers.containsString("aria-pressed=\"true\"")));
    }

    @Test
    void everyProjectListsItsKeyFeatures() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.containsString("Key Features")))
                .andExpect(content().string(Matchers.containsString("Vector-based semantic search using Chroma")))
                .andExpect(content().string(Matchers.containsString("HTMX partial page updates without full-page reloads")));
    }

    @Test
    void contactFormFallsBackToAPlainPostWhenHtmxIsAbsent() throws Exception {
        mvc.perform(post("/contact")
                        .param("name", "A C Recruiter")
                        .param("email", "hr@example.com")
                        .param("topic", "Job Opportunity")
                        .param("message", "We are rebuilding a payment platform and need architecture advice."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/#contact"));
    }

    @Test
    void invalidHtmxSubmissionReturnsTheErrorFragmentNotTheWholePage() throws Exception {
        String body = mvc.perform(post("/contact")
                        .header("HX-Request", "true")
                        .param("name", "E")
                        .param("email", "not-an-email")
                        .param("topic", "")
                        .param("message", "too short"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("id=\"contact-result\"")))
                .andExpect(content().string(Matchers.containsString("form-result is-error")))
                .andExpect(content().string(Matchers.containsString("hx-swap-oob=\"true\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("<html"))))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("That needs a second look.");
    }

    @Test
    void theErrorSlotsExistOnAPristinePageSoHtmxCanSwapThem() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.containsString("id=\"err-name\"")))
                .andExpect(content().string(Matchers.containsString("id=\"err-email\"")))
                .andExpect(content().string(Matchers.containsString("id=\"err-topic\"")))
                .andExpect(content().string(Matchers.containsString("id=\"err-message\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("is-error"))));
    }

    /**
     * The no-JS path is a redirect, so the rejected values have to travel through the flash
     * attributes; otherwise the user loses the message body they just typed.
     */
    @Test
    void invalidPlainPostRedirectsAndKeepsWhatTheUserTyped() throws Exception {
        MvcResult redirect = mvc.perform(post("/contact")
                        .param("name", "E")
                        .param("email", "not-an-email")
                        .param("topic", "")
                        .param("message", "short"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/#contact"))
                .andReturn();

        // Flash attributes live in the session, and are consumed by the first read.
        MockHttpSession session = (MockHttpSession) redirect.getRequest().getSession(false);

        mvc.perform(get("/").session(session))
                .andExpect(model().attribute("contactState", "error"))
                .andExpect(content().string(Matchers.containsString("field__error is-error")))
                .andExpect(content().string(Matchers.containsString("value=\"not-an-email\"")))
                .andExpect(content().string(Matchers.containsString("short</textarea>")));
    }

    @Test
    void validHtmxSubmissionReturnsTheSuccessFragment() throws Exception {
        mvc.perform(post("/contact")
                        .header("HX-Request", "true")
                        .param("name", "A C Recruiter")
                        .param("email", "hr@example.com")
                        .param("topic", "Job Opportunity")
                        .param("message", "We are rebuilding a payment platform and need architecture advice."))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("form-result is-success")))
                .andExpect(content().string(Matchers.containsString("Message sent.")));
    }

    @Test
    void honeypotIsAcceptedSilentlyInsteadOfLeakingABotSignal() throws Exception {
        mvc.perform(post("/contact")
                        .header("HX-Request", "true")
                        .param("name", "Spam Bot")
                        .param("email", "bot@spam.example")
                        .param("topic", "Job Opportunity")
                        .param("message", "Buy cheap backlinks at http://spam.example right now.")
                        .param("website", "http://spam.example"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("form-result is-success")));
    }

    @Test
    void theHeroShowsTheRealPhotograph() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.containsString("src=\"/img/faleelimg.jpeg\"")))
                .andExpect(content().string(Matchers.containsString(
                        "alt=\"Faleel H, Java developer and BCA graduate.\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("hero-portrait"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Portrait placeholder"))));
    }

    @Test
    void theNavToggleSurvivesFragmentInclusion() throws Exception {
        // Regression guard. th:replace on an icon include deletes the host <span>, which
        // silently threw away class="is-hidden" and the data-nav-icon-* hooks. The result was
        // two glyphs (hamburger AND cross) stuck side by side in the mobile menu button, and
        // site.js found null for both icons so the open/close swap never happened.
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("data-nav-icon-open")
                .contains("data-nav-icon-close")
                .contains("class=\"nav-toggle__icon is-hidden\"")
                // The templates are fully evaluated: no unprocessed th:* attribute may leak
                // into the response, which is what would happen if an include went wrong.
                .doesNotContain("th:insert")
                .doesNotContain("th:replace");
    }

    @Test
    void iconWrappersKeepTheirBoxStyling() throws Exception {
        // The same th:replace mistake was also dropping .icon-box, .info-column__icon and
        // .field__chevron, so those icons rendered as bare SVGs with no box behind them.
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("class=\"icon-box\"")
                .contains("class=\"info-column__icon\"")
                .contains("class=\"field__chevron\"");
    }

    @Test
    void theStatusPanelIsRealUiNotBakedIntoTheImage() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.containsString("class=\"status-overlay\"")))
                .andExpect(content().string(Matchers.containsString("Open to Work")))
                .andExpect(content().string(Matchers.containsString("Thiruvarur, India / Remote")));
    }

    @Test
    void thePlaceholderEndpointIsStillThere() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, This My Personal Website"));
    }

    @Test
    void deliveryStatusIsDistinguishableFromASuccessfulSend() {
        assertThat(new ContactDelivery(ContactDelivery.Status.LOGGED, "x").delivered()).isTrue();
        assertThat(new ContactDelivery(ContactDelivery.Status.SENT, "x").delivered()).isTrue();
        assertThat(new ContactDelivery(ContactDelivery.Status.FAILED, "x").delivered()).isFalse();
    }
}
