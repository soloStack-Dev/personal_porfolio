package com.example.demo.Service;

import com.example.demo.Model.ContactDelivery;
import com.example.demo.Model.ContactForm;
import com.example.demo.Model.ContactProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
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
            LOG.error("[contact] delivery failed for {}", maskLocalPart(form.getEmail()), ex);
            return new ContactDelivery(
                    ContactDelivery.Status.FAILED,
                    "The mail relay rejected the message. Please email faleelmr4@gmail.com directly.");
        }
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
}
