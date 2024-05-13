package ama.awesomemodeling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

@RestController
public class TestController {
    private final static String QUEUE_NAME = "hello";

    @Autowired
    private TestRepository repo;

    @GetMapping("/")
    String test() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbit");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                String message = "Hello World!";
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            System.out.println(e);
        }

        Person p = new Person("Alberto", "Basaglia");
        repo.save(p);

        return "Hello World!";
    }
}
