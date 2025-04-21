package org.protu.contentservice.progress.user;

import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.rabbit.RabbitMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventsConsumer {

  private final UserReplicaService userReplicaService;
  private final String USER_CREATED;
  private final String USER_UPDATED;
  private final String USER_DELETED;

  public UserEventsConsumer(UserReplicaService userReplicaService, AppProperties props) {
    this.userReplicaService = userReplicaService;
    USER_CREATED = props.rabbit().routingKey().userCreated();
    USER_UPDATED = props.rabbit().routingKey().userUpdated();
    USER_DELETED = props.rabbit().routingKey().userDeleted();
  }

  @RabbitListener(queues = "${app.rabbit.queue.user-replica}")
  public void consume(RabbitMessage<UserData> rabbitMessage, Message message) {
    final String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();

    if (receivedRoutingKey.equals(USER_CREATED)) {
      userReplicaService.addUser(rabbitMessage.data());

    } else if (receivedRoutingKey.equals(USER_UPDATED)) {
      userReplicaService.updateUserRoles(rabbitMessage.data());

    } else if (receivedRoutingKey.equals(USER_DELETED)) {
      userReplicaService.deleteUser(rabbitMessage.data().id());

    } else {
      throw new IllegalArgumentException("Unrecognized routing key: " + receivedRoutingKey);
    }
  }
}
