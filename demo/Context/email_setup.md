Spring Boot + HTMX Email Communication Setup
A practical guide for adding a website contact/enquiry form that sends visitor messages to the website owner's email
inbox.
1. Communication Flow
Website Visitor
 |
 v
HTMX Contact / Enquiry Form
 |
 | POST /contact
 v
Spring Boot Controller
 |
 v
EmailService
 |
 v
SMTP Provider
 |
 v
Website Owner's Email Inbox
Key idea: HTMX handles the form submission and partial-page response. Spring Boot receives the request and
sends the email through an SMTP provider.

2. Recommended Project Structure
src/main/java/com/example/app/
■■■ controller/
■ ■■■ ContactController.java
■■■ service/
■ ■■■ EmailService.java
■■■ dto/
■ ■■■ ContactRequest.java
■■■ config/
 ■■■ MailConfig.java
src/main/resources/
■■■ templates/
■ ■■■ contact.html
■ ■■■ fragments/
■ ■■■ contact-success.html
■ ■■■ contact-error.html
■■■ application.properties

3. Add Spring Boot Mail Dependency
For a Maven project, add the Spring Boot Mail starter:
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

# I am already added Java mail sender

use this mail to setup when user give input data to click send button the input datas go to this email id 

- faleelmr4@gmail.com

4. SMTP Configuration
Example using Gmail SMTP. The same Spring Mail approach can be adapted to other SMTP providers.
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_APP_PASSWORD} 
# later i check to add the email password 
spring.mail.properties.mail.smtp.auth=true

spring.mail.properties.mail.smtp.starttls.enable=true
Security: Do not commit your real email password or SMTP credentials to GitHub. Use environment variables or your
deployment platform's secret/environment-variable settings.
5. Environment Variables
MAIL_USERNAME=owner@example.com
MAIL_APP_PASSWORD=your-app-password
For Gmail, use an App Password where applicable rather than your normal account password. Keep the credentials
private.

6. Contact Request DTO
public class ContactRequest {
 private String name;
 private String email;
 private String message;
 public String getName() {
 return name;
 }
 public void setName(String name) {
 this.name = name;
 }
 public String getEmail() {
 return email;
 }
 public void setEmail(String email) {
 this.email = email;
 }
 public String getMessage() {
 return message;
 }
 public void setMessage(String message) {
 this.message = message;
 }
}


7. Email Service
@Service
public class EmailService {
 private final JavaMailSender mailSender;
 public EmailService(JavaMailSender mailSender) {
 this.mailSender = mailSender;
 }
 public void sendContactEmail(
 String visitorName,
 String visitorEmail,
 String message) {
 SimpleMailMessage mail = new SimpleMailMessage();
 mail.setTo("owner@example.com");
 mail.setSubject("New Website Enquiry");
 mail.setText(
 "New message received from website.\n\n" +
 "Name: " + visitorName + "\n" +
 "Email: " + visitorEmail + "\n\n" +
 "Message:\n" + message
 );
 mailSender.send(mail);
 }
}
8. Controller
@Controller
public class ContactController {
 private final EmailService emailService;
 public ContactController(EmailService emailService) {
 this.emailService = emailService;
 }
 @PostMapping("/contact")
 public String sendContact(ContactRequest request) {
 emailService.sendContactEmail(
 request.getName(),
 request.getEmail(),
 request.getMessage()
 );
 return "fragments/contact-success";
 }
}

9. HTMX Contact Form
<form
 hx-post="/contact"
 hx-target="#contact-result"
 hx-swap="innerHTML">
 <input
 type="text"
 name="name"
 placeholder="Your Name"
 required>
 <input
 type="email"
 name="email"
 placeholder="Your Email"
 required>
 <textarea
 name="message"
 placeholder="Your Message"
 required></textarea>
 <button type="submit">
 Send Message
 </button>
</form>
<div id="contact-result"></div>
10. HTMX Success Fragment
<div class="alert alert-success">
 Your message has been sent successfully.
</div>

11. Error Handling
In production, do not allow an SMTP exception to produce a raw server error page. Catch the email-sending failure,
log the technical error on the server, and return a friendly HTMX fragment.
try {
 emailService.sendContactEmail(
 request.getName(),
 request.getEmail(),
 request.getMessage()
 );
 return "fragments/contact-success";
} catch (MailException ex) {
 // Log the exception on the server.
 return "fragments/contact-error";
}
<div class="alert alert-danger">
 Unable to send your message right now.
 Please try again later.
</div>

12. Validation and Spam Protection
• Validate name, email, and message on the server, not only in HTML.
• Limit the maximum message length.
• Reject obviously invalid email addresses.
• Add rate limiting to prevent repeated automated submissions.
• Consider CAPTCHA or another anti-spam mechanism if the public form receives abuse.
• Never trust visitor-provided values when constructing HTML email content.
13. Plain Text vs HTML Email
The example uses SimpleMailMessage, which is suitable for plain-text notifications. If you need branded HTML
emails, use MimeMessage and MimeMessageHelper instead. HTML email is useful for a formatted enquiry
notification, but user-provided content must be handled safely.
14. Deployment Checklist
Item What to configure
SMTP host SMTP server provided by your email provider
SMTP port Usually 587 for STARTTLS or another provider-specific port
Username SMTP/email account
Password App password or SMTP credential stored as a secret
Owner email Destination address for website enquiries
Environment variables Configure them on the deployment platform
Domain/email authentication For production sending, configure provider-recommended DNS records where required

15. Recommended Production Architecture
Visitor
 |
 | HTMX POST /contact
 v
Spring Boot Controller
 |
 | validate request
 v
EmailService
 |
 | SMTP/API
 v
Transactional Email Provider
 |
 v
Website Owner Inbox
For a small personal/test website, SMTP can be enough. For a production business website with regular enquiries, a
transactional email provider can provide better deliverability, logs, domain authentication, and sending controls.

16. End-to-End Result
The visitor fills in the form and clicks Send Message. HTMX sends the form to Spring Boot without requiring a full
page reload. The Spring Boot service sends the enquiry through the configured mail provider, and the owner receives
the message in the configured inbox. HTMX then replaces the result area with a success or error message.
Note: Replace example addresses and credentials with your actual configuration. Never publish SMTP passwords, app passwords,
API keys, or other secrets in source control.