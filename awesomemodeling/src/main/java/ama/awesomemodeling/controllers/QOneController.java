package ama.awesomemodeling.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ama.awesomemodeling.services.ElasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ama.awesomemodeling.dtos.QOneCreateDTO;
import ama.awesomemodeling.entities.QOne;
import ama.awesomemodeling.entities.QTwoTopics;
import ama.awesomemodeling.entities.Topic;
import ama.awesomemodeling.enums.QOneStatus;
import ama.awesomemodeling.services.MalletService;
import ama.awesomemodeling.repositories.ControlRepository;

@RequestMapping(path="/q1", produces="application/json")
@RestController
@CrossOrigin(origins = "*")
public class QOneController {
    private final static String QUEUE_NAME = "q1";

    @Autowired
    private ControlRepository repo;

    @Autowired
    private MalletService malletService;

    @Autowired
    private ElasticService elasticService;

    @PostMapping("")
    ResponseEntity<QOne> post(@RequestBody QOneCreateDTO dto) {

        QOne qone = new QOne();

        qone.setTopic(dto.getTopic());
        qone.setStatus(QOneStatus.SUBMITTED);
        qone.setSubmitted_time(LocalDateTime.now());
        qone.setLocal_start_date(dto.getLocal_start_date());
        qone.setLocal_end_date(dto.getLocal_end_date());

        QOne saved = repo.save(qone);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbit");

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            // Integer id = saved.getId();
            // byte[] idbytes = ByteBuffer.allocate(4).putInt(id).array();
            channel.basicPublish("", QUEUE_NAME, null, saved.getId().getBytes());
            System.out.println(" [x] Sent '" + saved.getId() + "'");
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{collectionId}")
    ResponseEntity<QOne> getOne(@PathVariable(value = "collectionId") String id) {
        QOne qone = repo.findById(id).orElse(null);

        if (qone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(qone, HttpStatus.OK);

    }

    @DeleteMapping("/{collectionId}")
    ResponseEntity<QOne> deleteOne(@PathVariable(value = "collectionId") String id) {
        QOne qone = repo.findById(id).orElse(null);

        if (qone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        repo.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    ResponseEntity<List<QOne>> getAll() {
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{collectionId}/q2")
    ResponseEntity<QTwoTopics> get(@PathVariable(value = "collectionId") String id, @RequestParam String query,
            @RequestParam int k) {
        QOne qone = repo.findById(id).orElse(null);

        if (qone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ArrayList<String> articleContents = elasticService.retrieveDocuments("articles_" + id, "content", query);

        ArrayList<Topic> topics = malletService.getTopics(articleContents, k);
        if (topics == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new QTwoTopics(topics), HttpStatus.OK);
    }

}
