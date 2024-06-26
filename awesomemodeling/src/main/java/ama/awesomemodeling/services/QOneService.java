package ama.awesomemodeling.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ama.awesomemodeling.dtos.QOneCreateDTO;
import ama.awesomemodeling.entities.QOne;
import ama.awesomemodeling.enums.QOneStatus;
import ama.awesomemodeling.repositories.ControlRepository;

@Service
public class QOneService {
    public class NotFoundException extends Exception {
    }

    public class InProgressException extends Exception {
    }

    private final static String QUEUE_NAME = "q1";

    @Autowired
    private ControlRepository repo;

    public QOne createQOne(QOneCreateDTO dto) throws IOException, TimeoutException {
        QOne qone = new QOne();

        qone.setTopic(dto.getTopic());
        qone.setStatus(QOneStatus.SUBMITTED);
        qone.setSubmitted_time(LocalDateTime.now());
        qone.setLocal_start_date(dto.getLocal_start_date());
        qone.setLocal_end_date(dto.getLocal_end_date());

        QOne saved = repo.save(qone);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbit");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, saved.getId().getBytes());

        return saved;
    }

    public QOne getQOneById(String id) {
        return repo.findById(id).orElse(null);
    }

    public void deleteQOneBtId(String id) throws InProgressException, NotFoundException {
        QOne qone = this.getQOneById(id);

        if (qone == null) {
            throw new NotFoundException();
        }

        if (qone.getStatus() == QOneStatus.PROCESSING) {
            throw new InProgressException();
        }

        repo.deleteById(id);
    }

}
