package in.tkn.book_network.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // Inside the sendEmail method


    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {

        String templateName;
        if (emailTemplate != null) {
            templateName = emailTemplate.getName(); // Use the enum value
        } else {
            throw new IllegalArgumentException("Invalid email template specified");
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper  =new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );
        Map<String,Object> properties =new HashMap<>();
        properties.put("username",username);
        properties.put("confirmationURL",confirmationUrl);
        properties.put("activation_code",activationCode);
        logger.debug("Preparing to send email to: {}", to);
        logger.debug("Using template: {}", templateName);
        logger.debug("Email properties: {}", properties);
        Context context= new Context();
        context.setVariables(properties);

        messageHelper.setFrom("contact@tknBook.com");
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);

        String template =templateEngine.process(templateName,context);
        messageHelper.setText(template,true);

        mailSender.send(mimeMessage);

    }


}
