package ama.awesomemodeling.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ama.awesomemodeling.dtos.QOneCreateDTO;
import ama.awesomemodeling.entities.Article;
import ama.awesomemodeling.entities.Topic;
import ama.awesomemodeling.entities.QOne;
import ama.awesomemodeling.entities.QTwoTopics;
import ama.awesomemodeling.enums.QOneStatus;
import ama.awesomemodeling.repositories.ControlRepository;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@RequestMapping(path="/q1", produces="application/json")
@RestController
public class QOneController {
    private final static String QUEUE_NAME = "q1";

    @Autowired
    private ControlRepository repo;

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

        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @GetMapping("/{collectionId}")
    ResponseEntity<QOne> getOne(@PathVariable(value = "collectionId") String id) {
        QOne qone = repo.findById(id).orElse(null);

        if (qone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(qone, HttpStatus.OK);

    }

    @GetMapping("/")
    ResponseEntity<List<QOne>> getAll() {
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    public List<Hit<Article>> search(ElasticsearchClient esClient, String index, String field, String query)
            throws IOException {

        SearchResponse<Article> response = esClient.search(s -> s
                .index(index)
                .query(q -> q
                        .match(t -> t
                                .field(field)
                                .query(query))),
                Article.class);

        System.out.println("Searching done");
        return response.hits().hits();
    }

    @GetMapping("/{collectionId}/q2")
    ResponseEntity<QTwoTopics> get(@PathVariable(value = "collectionId") String id, @RequestParam String query,
            @RequestParam int k) {
        QOne qone = repo.findById(id).orElse(null);

        if (qone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // URL and API key
        String serverUrl = "http://elastic:9200";

        // Create the low-level client
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient esClient = new ElasticsearchClient(transport);

        // Perform the search
        ArrayList<String> articleContents = new ArrayList<>();
        try {
            List<Hit<Article>> hits = search(esClient, "articles_" + id, "content", query);
            if (hits.isEmpty()) {
                System.out.println("No articles found");
            } else {
                for (Hit<Article> hit : hits) {
                    Article article = hit.source();
                    System.out.println("Found article: " + article.getTitle());
                    articleContents.add(article.getContent());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                restClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create a list of pipes to process the documents
        ArrayList<Pipe> pipeList = new ArrayList<>();

        pipeList.add(new CharSequence2TokenSequence());
        pipeList.add(new TokenSequenceLowercase());
        pipeList.add(new TokenSequenceRemoveStopwords());
        pipeList.add(new TokenSequence2FeatureSequence());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        String[] articleContentsArray = articleContents.toArray(new String[0]);
        instances.addThruPipe(new StringArrayIterator(articleContentsArray));

        int numTopics = k;
        ParallelTopicModel model = new ParallelTopicModel(numTopics);
        model.addInstances(instances);

        model.setNumThreads(8);
        model.setNumIterations(100);

        System.out.println("Running the model");

        // Run the model
        try {
            model.estimate();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        System.out.println("Execution finished");

        Alphabet dataAlphabet = instances.getDataAlphabet();
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        ArrayList<Topic> topics = new ArrayList<>();

        for (int topic = 0; topic < numTopics; topic++) {
            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            out.format("Topic %d:\n", topic);
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            ArrayList<String> currentTopic = new ArrayList<>();
            int rank = 0;
            while (iterator.hasNext() && rank < 10) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                currentTopic.add((String) dataAlphabet.lookupObject(idCountPair.getID()));
                rank++;
            }
            topics.add(new Topic(currentTopic));
            System.out.println(out);
        }

        return new ResponseEntity<>(new QTwoTopics(topics), HttpStatus.OK);
    }

}
