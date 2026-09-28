package com.example.demo.Service;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Model.ContactProperties;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.util.StringUtils;

/**
 * Turns a validated {@link ContactForm} into an email.
 *
 * <p>Graceful by design: {@code JavaMailSender} only exists when {@code spring.mail.host} is set,
 * so with the stock configuration the service logs the enquiry instead of throwing. That keeps
 * {@code ./mvnw.cmd spring-boot:run} working out of the box while staying honest in the UI about
 * whether the message was actually delivered.
 */
@Service
public class ContactMailService {

    private static final Logger LOG = LoggerFactory.getLogger(ContactMailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ContactProperties properties;

    /** Guards the sender-address warning so it appears once, not once per submission. */
    private final AtomicBoolean senderAddressChecked = new AtomicBoolean();

    public ContactMailService(ObjectProvider<JavaMailSender> mailSenderProvider, ContactProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
    }

    public ContactDelivery submit(ContactForm form) {
        String subject = properties.subjectPrefix() + " — " + form.getTopic() + " from " + form.getName();

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOG.info("""
                    [contact] SMTP not configured (set spring.mail.host to enable delivery)
                      to:      {}
                      replyTo: {}
                      topic:   {}
                      message: {}""", properties.to(), form.getEmail(), form.getTopic(), form.getMessage());
            return new ContactDelivery(
                    ContactDelivery.Status.LOGGED,
                    "No mail relay is configured yet, so this enquiry was recorded in the server log only.");
        }

        warnOnceIfSenderAddressLooksUnconfigured();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(properties.from());
            helper.setTo(new InternetAddress(properties.to()));
            helper.setReplyTo(new InternetAddress(form.getEmail()));
            helper.setSubject(subject);

            // Spring Framework 7 dropped MimeMessageHelper#setHtmlBody, so the
            // multipart/alternative body is assembled directly.
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(plainText(form), "UTF-8");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody(form), "text/html; charset=UTF-8");

            MimeMultipart alternative = new MimeMultipart("alternative");
            alternative.addBodyPart(textPart);
            alternative.addBodyPart(htmlPart);
            message.setContent(alternative);

            mailSender.send(message);
            return new ContactDelivery(
                    ContactDelivery.Status.SENT,
                    "Delivered to the mailbox behind " + maskLocalPart(form.getEmail()) + ".");
        } catch (MailException | MessagingException | IllegalArgumentException ex) {
            if (isAuthenticationFailure(ex)) {
                // A rejected credential is a deployment fault, not a transient one: every
                // submission fails identically until the password is replaced, and the raw stack
                // trace buries the one line that matters. Gmail's code is explicit, so say what
                // to do about it. The password is deliberately not logged, and the visitor is
                // told nothing about the infrastructure.
                LOG.error("""
                        [contact] the relay REJECTED THE CREDENTIALS, so nothing was sent. This is \
                        a configuration fault, not a transient one: every submission will fail \
                        this way until the password is replaced. Nothing was sent to the relay, \
                        so the enquiry is lost - ask the sender to email directly.
                          Gmail replies 534-5.7.9 "Application-specific password required" when an \
                        account password is used. Create an App Password instead: Google Account \
                        -> Security -> 2-Step Verification -> App passwords -> Mail -> Generate. \
                        It is 16 lowercase letters with no symbols. Put it in \
                        application-secret.properties as spring.mail.password. No client id or \
                        client secret is involved; those are OAuth2 credentials and are not used \
                        when sending as yourself.""");
            } else {
                LOG.error("[contact] delivery failed for {}", maskLocalPart(form.getEmail()), ex);
            }
            return new ContactDelivery(
                    ContactDelivery.Status.FAILED,
                    "The mail relay rejected the message. Please email " + properties.to() + " directly.");
        }
    }

    /**
     * Whether the failure was a rejected username or password.
     *
     * <p>Checked by walking the cause chain because Spring wraps the Jakarta exception in
     * {@link org.springframework.mail.MailAuthenticationException}, and the wrapping is not
     * uniform: a failure during {@code protocolConnect} surfaces differently from one raised by
     * {@code doSend}. Package-private and static so it can be asserted without a live relay.
     */
    static boolean isAuthenticationFailure(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof AuthenticationFailedException) {
                return true;
            }
            if (current == current.getCause()) {
                break;
            }
        }
        return false;
    }

    private String plainText(ContactForm form) {
        return "Name:    " + form.getName() + "\n"
                + "Email:   " + form.getEmail() + "\n"
                + "Subject: " + form.getTopic() + "\n\n"
                + form.getMessage() + "\n";
    }

    private String htmlBody(ContactForm form) {
        return """
                <div style="font-family:Inter,Segoe UI,Helvetica,Arial,sans-serif;font-size:14px;line-height:1.6;color:#0B0F19">
                  <p style="margin:0 0 14px;color:#6D28D9;font-size:11px;letter-spacing:.12em;text-transform:uppercase">
                    Portfolio message</p>
                  <table cellpadding="0" cellspacing="0" style="border-collapse:collapse;width:100%%">
                    <tr><td width="90" style="padding:4px 0;color:#64748B;font-size:12px">Name</td>
                        <td style="padding:4px 0;font-weight:600">%s</td></tr>
                    <tr><td style="padding:4px 0;color:#64748B;font-size:12px">Email</td>
                        <td style="padding:4px 0"><a href="mailto:%s" style="color:#6D28D9">%s</a></td></tr>
                    <tr><td style="padding:4px 0;color:#64748B;font-size:12px">Subject</td>
                        <td style="padding:4px 0;font-weight:600">%s</td></tr>
                  </table>
                  <p style="margin:16px 0 0;padding:14px 16px;background:#FAF9F6;border:1px solid #E8E0D5;border-radius:12px">%s</p>
                </div>
                """.formatted(
                escape(form.getName()),
                escape(form.getEmail()),
                escape(form.getEmail()),
                escape(form.getTopic()),
                escape(form.getMessage()).replace("\n", "<br>"));
    }

    private String escape(String raw) {
        return HtmlUtils.htmlEscape(raw == null ? "" : raw);
    }

    private String maskLocalPart(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 1) {
            return "your inbox";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /**
     * Warns once if a relay is configured but the sender address is still the stock placeholder.
     *
     * <p>{@code app.contact.from} has to match the authenticated mailbox or be an alias on it, and
     * a relay rejects the whole message with {@code 550 The specified from address does not match
     * a permitted sender} otherwise - which happens after authentication succeeds, so fixing the
     * password alone would still leave every send failing. The record's default is a placeholder,
     * so forgetting the key is easy and the resulting error is not obviously about configuration.
     * Logged once rather than per submission, since it never changes between requests.
     */
    private void warnOnceIfSenderAddressLooksUnconfigured() {
        if (!senderAddressChecked.compareAndSet(false, true)) {
            return;
        }
        if (ContactProperties.PLACEHOLDER_SENDER.equalsIgnoreCase(properties.from())) {
            LOG.warn("""
                    [contact] app.contact.from is still the placeholder "{}". A relay requires the \
                    From address to match the authenticated mailbox or be an alias on it, so every \
                    send will be rejected with 550 even once the password is fixed. Set it beside \
                    the credentials in application-secret.properties.""",
                    ContactProperties.PLACEHOLDER_SENDER);
        }
    }
}
