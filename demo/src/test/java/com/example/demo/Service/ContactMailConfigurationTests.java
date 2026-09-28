package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Model.ContactProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Proves how {@code JavaMailSender} is actually wired, in both directions.
 *
 * <p>These exist because the failure mode here is silent. An unrecognised
 * {@code spring.mail.*} key does not fail the build and does not log a warning — Spring Boot
 * ignores it, so {@code spring.mail.starttls=true} or {@code spring.mail.smtp.auth=true} simply
 * never reach the sender and the send fails later, against a live relay, where it is expensive to
 * diagnose. Asserting the assembled sender is the only way to catch that here.
 */
class ContactMailConfigurationTests {

    private static ContactForm form() {
        ContactForm form = new ContactForm();
        form.setName("A Recruiter");
        form.setEmail("recruiter@example.com");
        form.setTopic("Job Opportunity");
        form.setMessage("We would like to talk about a backend role.");
        return form;
    }

    /** Minimal context: only the mail autoconfiguration under test, not the whole web app. */
    @SpringBootApplication
    @EnableConfigurationProperties(ContactProperties.class)
    static class MailOnlyApp {

        @Bean
        ContactMailService contactMailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                              ContactProperties properties) {
            return new ContactMailService(mailSenderProvider, properties);
        }
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(MailOnlyApp.class);

    @Test
    void withNoHostThereIsNoSenderAndTheEnquiryIsLoggedInstead() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            // This absence is the whole no-config fallback: ContactMailService sees null and logs.
            assertThat(context.getBeanProvider(JavaMailSender.class).getIfAvailable()).isNull();
            assertThat(context.getBean(ContactMailService.class).submit(form()).status())
                    .isEqualTo(ContactDelivery.Status.LOGGED);
        });
    }

    @Test
    void anEmptyHostIsStillPresentAndMustNotBeUsedToDisableMail() {
        // Documents the trap: spring.mail.host="" is a *present* host, so a JavaMailSender is
        // created. This is why .env.example comments the variable out instead of blanking it.
        runner.withPropertyValues("spring.mail.host=").run(context ->
                assertThat(context.getBeanProvider(JavaMailSender.class).getIfAvailable()).isNotNull());
    }

    @Test
    void withAHostTheSenderCarriesTheCredentialsAndTheTransportSettings() {
        runner.withPropertyValues(
                        "spring.mail.host=smtp.example.test",
                        "spring.mail.port=587",
                        "spring.mail.username=you@example.test",
                        "spring.mail.password=secret",
                        "spring.mail.properties.mail.smtp.auth=true",
                        "spring.mail.properties.mail.smtp.starttls.enable=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    JavaMailSenderImpl sender = (JavaMailSenderImpl) context.getBean(JavaMailSender.class);

                    assertThat(sender.getHost()).isEqualTo("smtp.example.test");
                    assertThat(sender.getPort()).isEqualTo(587);
                    assertThat(sender.getUsername()).isEqualTo("you@example.test");
                    assertThat(sender.getPassword()).isEqualTo("secret");

                    // The two dotted keys, which an environment variable cannot supply.
                    assertThat(sender.getJavaMailProperties())
                            .containsEntry("mail.smtp.auth", "true")
                            .containsEntry("mail.smtp.starttls.enable", "true");
                });
    }

    @Test
    void aRelayThatRefusesTheConnectionReportsFailureRatherThanClaimingSuccess() {
        // Port 1 on localhost is closed, so this needs no network and no credentials. The point
        // is that a misconfigured relay surfaces as FAILED and the UI says so, not as a lie.
        runner.withPropertyValues(
                        "spring.mail.host=127.0.0.1",
                        "spring.mail.port=1",
                        "spring.mail.test-connection=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    ContactDelivery delivery = context.getBean(ContactMailService.class).submit(form());
                    assertThat(delivery.status()).isEqualTo(ContactDelivery.Status.FAILED);
                    assertThat(delivery.detail()).contains("faleelmr4@gmail.com");
                    assertThat(delivery.delivered()).isFalse();
                });
    }

    @Test
    void testConnectionTrueRefusesToStartWhenTheRelayIsUnreachable() {
        // Guards the switch itself, so flipping it on in production is a deliberate act that
        // fails loudly rather than silently.
        runner.withPropertyValues(
                        "spring.mail.host=127.0.0.1",
                        "spring.mail.port=1",
                        "spring.mail.test-connection=true")
                .run(context -> assertThat(context).hasFailed());
    }
}
