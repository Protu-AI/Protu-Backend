package org.protu.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.dto.RabbitMQMessage;
import org.protu.notificationservice.helper.TemplateProcessor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  public void sendEmail(String to, String subject, String body) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

    messageHelper.setFrom("protu_ai@gmail.com");
    messageHelper.setTo(to);
    messageHelper.setSubject(subject);
    messageHelper.setText(body, true);
    mailSender.send(mimeMessage);
  }

  public void prepareAndSendEmail(RabbitMQMessage message, TemplateProcessor templateProcessor) throws MessagingException {
    Map<String, Object> variables = templateProcessor.getVariables(message);
    String htmlBody = templateProcessor.loadTemplate(variables);
    sendEmail(message.to(), "Verify your email", htmlBody);
  }
}
