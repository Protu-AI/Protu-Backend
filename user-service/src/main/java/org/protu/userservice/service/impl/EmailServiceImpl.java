package org.protu.userservice.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;

  @Async
  @Override
  public void sendEmail(String to, String subject, String body) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

    messageHelper.setFrom("protu_ai@gmail.com");
    messageHelper.setTo(to);
    messageHelper.setSubject(subject);
    messageHelper.setText(body, true);

    mailSender.send(mimeMessage);
  }
}
