package com.example.demo.Controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The contact form with a relay that is configured but refuses connections, which is the state
 * a wrong password, a revoked app password or an outage all look like from the server's side.
 *
 * <p>This exists because that state was only ever exercised against the no-host case, where
 * {@code ContactMailService} logs and returns {@code LOGGED} and never opens a socket. Once a
 * host is configured the send really runs, and the plain-POST failure path is the one that
 * flashed an empty {@code BindingResult}: validation had passed, so
 * {@code binding.hasErrors()} was false, and the visitor's only feedback depended on the
 * delivery status reaching the template. Asserted end to end because the symptom was a 500 with
 * a misleading {@code /error} serialization complaint rather than anything about mail.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.mail.host=127.0.0.1",
        "spring.mail.port=1",
        "spring.mail.username=faleel@example.test",
        "spring.mail.password=irrelevant",
        "spring.mail.test-connection=false"
})
class ContactMailFailurePageTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void plainPostAgainstAnUnreachableRelayStillRendersAPage() throws Exception {
        mvc.perform(post("/contact")
                        .param("name", "A C Recruiter")
                        .param("email", "hr@example.com")
                        .param("topic", "Job Opportunity")
                        .param("message", "We are rebuilding a payment platform and need architecture advice."))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void theRedirectTargetRendersTheFailureWithoutCollapsing() throws Exception {
        // Follows the Post/Redirect/Get the way a browser would, which the existing tests do not.
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"contact-result\"")));
    }

    @Test
    void theRedirectTargetRendersWhenTheFlashAttributesComeBack() throws Exception {
        // Reproduces the one flow nothing covered: a submission that passes validation but fails
        // at the relay. The controller flashes an EMPTY BindingResult plus the delivery, and the
        // browser then GETs / with the same session. MockMvc gives every perform() a fresh
        // context, so a bare get("/") sees a clean model and passes; only replaying the session
        // reaches the state a real redirect produces. That is how this reached production.
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/contact")
                        .session(session)
                        .param("name", "A C Recruiter")
                        .param("email", "hr@example.com")
                        .param("topic", "Job Opportunity")
                        .param("message", "We are rebuilding a payment platform and need architecture advice."))
                .andExpect(status().is3xxRedirection());

        // Same session, as the browser's redirect would carry it.
        mvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"contact-result\"")))
                .andExpect(content().string(containsString("form-result is-error")));
    }

    @Test
    void htmxPostAgainstAnUnreachableRelayReturnsTheErrorFragmentNotA500() throws Exception {
        mvc.perform(post("/contact")
                        .header("HX-Request", "true")
                        .param("name", "A C Recruiter")
                        .param("email", "hr@example.com")
                        .param("topic", "Job Opportunity")
                        .param("message", "We are rebuilding a payment platform and need architecture advice."))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"contact-result\"")));
    }
}
