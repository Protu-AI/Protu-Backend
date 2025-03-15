package org.protu.notificationservice.helper;

import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.dto.RabbitMQMessage;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailVerificationTemplateProcessor implements TemplateProcessor {

  private final TemplateEngine templateEngine;

  @Override
  public Map<String, Object> getVariables(RabbitMQMessage message) {
    Map<String, Object> variables = new HashMap<>();
    variables.put("username", message.template().data().username());
    variables.put("otpTtl", message.template().data().otpTtl());

    String otp = message.template().data().otp();
    for (int i = 0; i < otp.length(); i++) {
      variables.put("otp_" + (i + 1), otp.charAt(i));
    }

    return variables;
  }

  @Override
  public String loadTemplate(Map<String, Object> variables) {
    Context context = new Context();
    variables.forEach(context::setVariable);
    return templateEngine.process("email-verification", context);
  }
}
