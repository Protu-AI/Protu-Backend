package org.protu.notificationservice.helper;

import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.dto.EmailData;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PasswordResetTemplateProcessor implements TemplateProcessor {

  private final TemplateEngine templateEngine;

  @Override
  public Map<String, Object> getVariables(EmailData emailData) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("username", emailData.username());
    variables.put("otpTtl", emailData.otp().ttlInMinutes());

    String otp = emailData.otp().value();
    for (int i = 0; i < otp.length(); i++) {
      variables.put("otp_" + (i + 1), otp.charAt(i));
    }

    return variables;
  }

  @Override
  public String loadTemplate(Map<String, Object> variables) {
    Context context = new Context();
    variables.forEach(context::setVariable);
    return templateEngine.process("password-reset", context);
  }
}
