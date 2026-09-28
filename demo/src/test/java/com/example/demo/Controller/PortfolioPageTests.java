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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
                // Every project link is a real anchor, and the Résumé — the one link with no
                // destination — is a <span> in the hero, not a dead href="#" and not a button
                // that only admits it when clicked. No data-* hook is left for a script to find.
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-placeholder-link"))))
                .andExpect(content().string(Matchers.containsString("class=\"link-action link-action--unavailable\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-copy"))))
                .andExpect(content().string(Matchers.containsString("mailto:faleelmr4@gmail.com")))
                .andExpect(content().string(Matchers.containsString("not available yet")));
    }

    @Test
    void projectFilterPillsAreLinksAndTheServerDoesTheFiltering() throws Exception {
        // No script needs to exist for this to work, so the filter is plain links plus a
        // server-side selection: the pills are anchors, the active one is marked for assistive
        // tech, and the count is real text in the response rather than a JS-written string.
        mvc.perform(get("/"))
                .andExpect(model().attributeExists(
                        "projects", "filters", "activeFilterId", "activeFilterLabel",
                        "projectCount", "projectCountLabel"))
                .andExpect(model().attribute("activeFilterId", "all"))
                .andExpect(content().string(Matchers.containsString("href=\"/?filter=ai\"")))
                .andExpect(content().string(Matchers.containsString("aria-current=\"true\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("aria-pressed"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-filter="))))
                .andExpect(content().string(Matchers.containsString("2 projects · All")));
    }

    @Test
    void filteringByCategoryReturnsOnlyTheMatchingCards() throws Exception {
        // ?filter=ai must narrow the response itself, not merely mark a pill: only the RAG
        // project survives, and the status line reports one project. This is the regression
        // that would catch the filter being re-implemented on the client.
        mvc.perform(get("/").param("filter", "ai"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeFilterId", "ai"))
                .andExpect(model().attribute("projectCount", 1))
                .andExpect(content().string(Matchers.containsString("1 project ·")))
                .andExpect(content().string(Matchers.containsString("rag-enterprise")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-entry-management"))));
    }

    @Test
    void anUnknownFilterFallsBackToShowingEverything() throws Exception {
        // A stale bookmark or a hand-edited URL must not render an empty page.
        mvc.perform(get("/").param("filter", "no-such-filter"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeFilterId", "all"))
                .andExpect(model().attribute("projectCount", 2))
                .andExpect(content().string(Matchers.containsString("2 projects · All")));
    }

    @Test
    void theSkillsListsStateWhatIsActuallyUsed() throws Exception {
        // The 02 / CAPABILITIES cards are a claim about what this site is built with, so they
        // are asserted rather than left to drift. Core Languages is Java, SQL, HTMX; the
        // languages this project does not use must not come back. Backend & Web gained Nginx
        // and lost jQuery. Asserting the removal is the part that matters, because a
        // "contains" check alone would pass even if someone re-added TypeScript later.
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains(">Java</li>")
                .contains(">SQL</li>")
                .contains(">HTMX</li>")
                .contains(">Nginx</li>");

        // None of these are used anywhere in this project, so advertising them would be fiction.
        for (String removed : new String[] {"JavaScript", "TypeScript", "jQuery"}) {
            assertThat(html).doesNotContain(">" + removed + "</li>");
        }
    }

    @Test
    void eachProjectCardStatesHowItCameToBeAndTheToneIsStyled() throws Exception {
        // statusTone is concatenated into a class name in the template, so an unrecognised value
        // fails silently and the dot loses its colour. This asserts the label, the class, and
        // that the CSS actually defines that tone.
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();
        String css = Files.readString(
                Path.of("src/main/resources/static/css/site.css"), StandardCharsets.UTF_8);

        // The data-entry platform was designed and built independently, so it says so.
        assertThat(html)
                .contains("Self-Initiated Project")
                .contains("class=\"status status--self\"")
                .contains("designed and built independently");

        // The RAG project is still the academic one.
        assertThat(html).contains("class=\"status status--academic\"");

        // Every tone rendered must have a rule, or the dot silently loses its colour.
        for (String tone : List.of("live", "metric", "teams", "academic", "self", "installs")) {
            assertThat(css).as("site.css must define .status--" + tone).contains(".status--" + tone);
        }
    }

    @Test
    void theDarkThemeIsACheckboxAndEveryTokenItOverridesExistsInTheLightTheme() throws Exception {
        // The dark theme is one block of custom-property overrides selected by
        // body:has(#theme-toggle:checked). Two ways this silently rots, both asserted here:
        // a component using a literal colour instead of a token, which no theme can reach; and
        // an override naming a token that :root never defines, which does nothing at all.
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();
        // Normalise line endings: the checked-out stylesheet uses CRLF on Windows, so any
        // multi-line literal in this test would otherwise fail only on this platform.
        String css = Files.readString(
                Path.of("src/main/resources/static/css/site.css"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n");

        assertThat(html)
                .contains("id=\"theme-toggle\"")
                .contains("class=\"theme-toggle-input\"")
                .contains("for=\"theme-toggle\"")
                .contains("theme-toggle__icon--light")
                .contains("theme-toggle__icon--dark")
                .contains("Switch between dark and light theme");

        // Both glyphs ship in the DOM and CSS swaps them, so there must be no data-* or
        // inline-script fallback to go stale.
        assertThat(html).doesNotContain("data-theme-icon");

        // Locate the real rule, not the comment above it that also spells out the selector:
        // require the declaration to follow the opening brace on its own line.
        int darkStart = css.indexOf("body:has(#theme-toggle:checked) {\n  color-scheme: dark;");
        assertThat(darkStart).as("the dark token block must exist").isPositive();
        String darkBlock = css.substring(darkStart, css.indexOf("\n}", darkStart));

        // Every --token overridden in dark mode must be defined in :root, or it inherits
        // nothing and the override is a no-op.
        String root = css.substring(css.indexOf(":root {"), css.indexOf("}", css.indexOf(":root {")));
        for (String token : List.copyOf(
                java.util.regex.Pattern.compile("--[a-z0-9-]+(?=\\s*:)")
                        .matcher(darkBlock).results().map(java.util.regex.MatchResult::group).toList())) {
            assertThat(root).as("dark theme overrides " + token).contains(token + ":");
        }
    }

    @Test
    void theCustomJavaScriptIsGoneAndOnlyHtmxRemains() throws Exception {
        // The whole point of the refactor: no site.js is referenced, and the only script left
        // is htmx, which the contact form degrades from without. Asserting on the URL rather
        // than the bare name, because the templates document the removal in a comment.
        mvc.perform(get("/"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/js/site.js"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"toast-host\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-to-top"))))
                .andExpect(content().string(Matchers.containsString("htmx.org")));
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
    void theMobileDrawerIsACheckboxDrivenByCssNotByAScript() throws Exception {
        // The drawer used to be a <button> plus a JS class toggle, and a second <span> glyph
        // that JS swapped in and out. It is now a real checkbox: the browser does the toggling
        // and the state is announced natively. The two things that matter structurally are
        // that the input is a preceding sibling of .site-nav (the CSS depends on
        // #nav-toggle:checked ~ .site-nav) and that there is only ever one glyph in the toggle.
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();

        assertThat(html)
                .contains("id=\"nav-toggle\"")
                .contains("class=\"nav-toggle-input\"")
                .contains("aria-controls=\"site-nav\"")
                .contains("class=\"nav-toggle\"")
                // The old JS-only hooks must be gone, and with them the duplicate-icon bug.
                .doesNotContain("data-nav-icon-open")
                .doesNotContain("data-nav-icon-close")
                .doesNotContain("nav-toggle__icon is-hidden")
                .doesNotContain("th:insert")
                .doesNotContain("th:replace");

        assertThat(html.indexOf("id=\"nav-toggle\""))
                .as("the checkbox must precede .site-nav for the ~ sibling selector to work")
                .isLessThan(html.indexOf("id=\"site-nav\""));
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
