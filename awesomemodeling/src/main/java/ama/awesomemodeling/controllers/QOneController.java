package ama.awesomemodeling.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
import ama.awesomemodeling.services.QOneService;
import ama.awesomemodeling.repositories.ControlRepository;

@RequestMapping(path="/q1", produces="application/json")
@RestController
@CrossOrigin(origins = "*")
public class QOneController {

    @Autowired
    private ControlRepository repo;

    @Autowired
    private MalletService malletService;

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private QOneService qOneService;

    @PostMapping("")
    ResponseEntity<QOne> post(@RequestBody QOneCreateDTO dto) {
        try {
            QOne saved = qOneService.createQOne(dto);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IOException | TimeoutException io) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{collectionId}")
    ResponseEntity<QOne> getOne(@PathVariable(value = "collectionId") String id) {
        QOne qone = qOneService.getQOneById(id);

        if (qone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(qone, HttpStatus.OK);

    }

    @DeleteMapping("/{collectionId}")
    ResponseEntity<Void> deleteOne(@PathVariable(value = "collectionId") String id) {

        try {
            qOneService.deleteQOneBtId(id);
        } catch (QOneService.InProgressException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (QOneService.NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("")
    ResponseEntity<List<QOne>> getAll() {
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{collectionId}/q2")
    ResponseEntity<QTwoTopics> get(@PathVariable(value = "collectionId") String id, @RequestParam String query,
            @RequestParam int k) {
        QOne qone = this.qOneService.getQOneById(id);

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
