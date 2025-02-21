package com.hazem.notificationservice.helper;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
public class TemplateProcessor {
  public String processTemplate(String template, Map<String, Object> variables) {
    TemplateEngine templateEngine = new TemplateEngine();
    StringTemplateResolver resolver = new StringTemplateResolver();
    templateEngine.setTemplateResolver(resolver);
    Context context = new Context();
    variables.forEach(context::setVariable);
    return templateEngine.process(template, context);
  }
}
