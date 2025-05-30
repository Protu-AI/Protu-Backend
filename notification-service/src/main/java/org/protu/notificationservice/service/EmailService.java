package org.protu.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.protu.notificationservice.dto.EmailData;
import org.protu.notificationservice.helper.TemplateProcessor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailService {
  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmail(String to, String subject, String body) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

    messageHelper.setFrom("protu_ai@gmail.com");
    messageHelper.setTo(to);
    messageHelper.setSubject(subject);
    messageHelper.setText(body, true);
    mailSender.send(mimeMessage);
  }

  public void prepareAndSendEmail(EmailData emailData, TemplateProcessor templateProcessor) throws MessagingException {
    Map<String, Object> variables = templateProcessor.getVariables(emailData);
    String htmlBody = templateProcessor.loadTemplate(variables);
    sendEmail(emailData.to(), "Verify your email", htmlBody);
  }
}
