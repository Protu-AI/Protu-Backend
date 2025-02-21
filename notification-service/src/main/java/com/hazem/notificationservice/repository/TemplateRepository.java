package com.hazem.notificationservice.repository;

import com.hazem.notificationservice.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {}
