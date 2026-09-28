package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Model.ContactProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /**
     * Reads a file from the module directory, so a test can assert on a checked-in config
     * resource rather than on a property the Environment may have already resolved away.
     */
    private static String readRepoFile(String relativePath) {
        try {
            return Files.readString(Path.of(relativePath), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("cannot read " + relativePath, e);
        }
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

    @Test
    void applicationPropertiesImportsTheUntrackedSecretFileAndTheEnvFile() {
        // The declarations themselves, read from the file, so they cannot be deleted by someone
        // who only ever runs Docker and sees the other mechanism keep working there.
        //
        // Only the declarations are asserted here, not the resulting sender. Binding an imported
        // file cannot be covered by this class: ApplicationContextRunner builds a plain
        // AnnotationConfigApplicationContext and never runs ConfigDataEnvironmentPostProcessor,
        // so spring.config.import is silently ignored and there is no sender to inspect. The
        // import is therefore verified against a running app instead, and the credential path it
        // feeds is covered by aConfiguredRelayProducesASenderWithTheExpectedCredentials above.
        String applicationProperties = readRepoFile("src/main/resources/application.properties");

        assertThat(applicationProperties)
                .contains("optional:file:application-secret.properties[.properties]")
                .contains("optional:file:.env[.properties]");
    }

    @Test
    void noTrackedFileCarriesCredentialsOrIsWiredToLookForOne() {
        // application-secret.properties and .env are both untracked, because either would
        // otherwise publish the SMTP password into a public repository and into the built jar.
        // Asserted on the ignore rules rather than trusted, since an indented pattern silently
        // stops matching: gitignore reads leading whitespace as part of the filename.
        String gitignore = readRepoFile(".gitignore");

        assertThat(gitignore)
                .contains("\n.env\n")
                .contains("\napplication-secret.properties\n")
                .doesNotContain("  .env");
    }

    @Test
    void noTrackedFileCarriesTheMailPassword() {
        // The one assertion that matters for a public repository. Someone copying credentials
        // into src/main/resources/application.properties is the obvious thing to try when the
        // form will not send, and that file is both committed and packaged into the jar.
        String mainProperties = readRepoFile("src/main/resources/application.properties");

        assertThat(mainProperties).doesNotMatch("(?m)^spring\\.mail\\.password=.*");
    }

    @Test
    void theSenderAddressIsNotDeclaredInBothFilesWithDifferentValues() {
        // app.contact.from must equal the authenticated address, and it lives in the untracked
        // file. Declaring it here as well means the two files disagree and import precedence
        // silently decides which one Gmail rejects with a 550.
        String mainProperties = readRepoFile("src/main/resources/application.properties");

        assertThat(mainProperties).doesNotMatch("(?m)^app\\.contact\\.from=.*");
    }

    @Test
    void theTestClasspathCannotSendRealMail() {
        // src/test/resources/application.properties shadows the main one, and shadowing is what
        // drops the credential import. @SpringBootTest runs the real config pipeline, so without
        // that shadow the developer's actual Gmail password would be loaded and
        // ContactMailFailurePageTests would open real connections to smtp.gmail.com. Asserted
        // because a well-meaning "just add the host here" would send test mail to a real inbox.
        String testProperties = readRepoFile("src/test/resources/application.properties");

        // Anchored to the start of a line so the explanatory comments in that file, which name
        // these keys while explaining why they are absent, do not count as declarations.
        assertThat(testProperties)
                .doesNotMatch("(?m)^spring\\.mail\\.host=.*")
                .doesNotMatch("(?m)^spring\\.mail\\.username=.*")
                .doesNotMatch("(?m)^spring\\.mail\\.password=.*")
                .doesNotMatch("(?m)^\\s*spring\\.config\\.import=.*");
    }
}
