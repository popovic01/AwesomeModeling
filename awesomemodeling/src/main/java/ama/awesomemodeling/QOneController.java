package ama.awesomemodeling;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rabbitmq.client.ConnectionFactory;

import ama.awesomemodeling.entities.QOne;
import ama.awesomemodeling.entities.QOneCreateDTO;
import ama.awesomemodeling.entities.QOneStatus;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

@RequestMapping("/q1")
@RestController
public class QOneController {
    private final static String QUEUE_NAME = "q1";

    @Autowired
    private ControlRepository repo;

    @PostMapping("/")
    String post(@RequestBody QOneCreateDTO dto) {

        QOne qone = new QOne();

        qone.setTopic(dto.getTopic());
        qone.setStatus(QOneStatus.SUBMITTED);
        qone.setSubmitted_time(LocalDateTime.now());

        QOne saved = repo.save(qone);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbit");

        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            // Integer id = saved.getId();
            // byte[] idbytes = ByteBuffer.allocate(4).putInt(id).array();
            channel.basicPublish("", QUEUE_NAME, null, saved.getId().getBytes());
            System.out.println(" [x] Sent '" + saved.getId()+ "'");
        } catch (Exception e) {
            System.out.println(e);
        }

        return "Starting document retrieval";
    }
    
}
