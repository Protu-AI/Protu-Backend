package org.protu.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.dto.RabbitMQMessage;
import org.protu.notificationservice.helper.TemplateProcessor;
import org.protu.notificationservice.model.Template;
import org.protu.notificationservice.repository.TemplateRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;
  private final TemplateRepository templateRepository;
  private final TemplateProcessor templateProcessor;

  private String prepareEmailBody(RabbitMQMessage rabbitMQMessage, String html) {
    Map<String, Object> variables = Map.of(
        "username", rabbitMQMessage.template().data().username(),
        "otp_1", rabbitMQMessage.template().data().otp().charAt(0),
        "otp_2", rabbitMQMessage.template().data().otp().charAt(1),
        "otp_3", rabbitMQMessage.template().data().otp().charAt(2),
        "otp_4", rabbitMQMessage.template().data().otp().charAt(3),
        "otp_5", rabbitMQMessage.template().data().otp().charAt(4),
        "otp_6", rabbitMQMessage.template().data().otp().charAt(5),
        "otpTtl", rabbitMQMessage.template().data().otpTtl());
    return templateProcessor.processTemplate(html, variables);
  }

  private void sendEmail(String to, String subject, String body) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

    messageHelper.setFrom("protu_ai@gmail.com");
    messageHelper.setTo(to);
    messageHelper.setSubject(subject);
    messageHelper.setText(body, true);
    mailSender.send(mimeMessage);
  }

  public void prepareAndSendEmail(RabbitMQMessage rabbitMQMessage) throws MessagingException {
    Template template = templateRepository.findById(rabbitMQMessage.template().id())
        .orElseThrow(() -> new RuntimeException("Template not found!"));

    String body = prepareEmailBody(rabbitMQMessage, template.getBody());
    sendEmail(rabbitMQMessage.to(), template.getSubject(), body);
  }
}
