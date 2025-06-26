package org.protu.userservice;

import com.github.f4b6a3.ulid.UlidCreator;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.dto.rabbitmq.RabbitMessage;
import org.protu.userservice.dto.rabbitmq.UserData;
import org.protu.userservice.model.User;
import org.protu.userservice.producer.UserEventsProducer;
import org.protu.userservice.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableConfigurationProperties(AppProperties.class)
@SpringBootApplication
public class UserServiceApplication {
  private final UserRepository users;
  private final AppProperties props;
  private final PasswordEncoder encoder;
  private final UserEventsProducer producer;

  public UserServiceApplication(UserRepository users, AppProperties props, PasswordEncoder encoder, UserEventsProducer producer) {
    this.users = users;
    this.props = props;
    this.encoder = encoder;
    this.producer = producer;
  }

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void addAdminUser() {
    if (users.findByUsername("admin").isPresent()) {
      return;
    }

    User user = User.builder()
        .publicId(UlidCreator.getUlid().toString())
        .username("admin")
        .email("admin@example.com")
        .firstName("admin_first")
        .lastName("admin_last")
        .roles("ROLE_USER,ROLE_ADMIN")
        .isEmailVerified(true)
        .isActive(true)
        .phoneNumber("123456789")
        .password(encoder.encode(props.admin().password()))
        .build();

    user = users.save(user);

    var USER_CREATED = props.rabbit().routingKey().userCreated();
    producer.send(
        new RabbitMessage<>(
            USER_CREATED, "event",
            new UserData(user.getId(), user.getPublicId(), user.getRoles())
        ), USER_CREATED);
  }

}
