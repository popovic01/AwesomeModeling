package ama.awesomemodeling.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.rabbitmq.client.ConnectionFactory;

import ama.awesomemodeling.entities.Article;
import ama.awesomemodeling.entities.QOne;
import ama.awesomemodeling.dtos.QOneCreateDTO;
import ama.awesomemodeling.enums.QOneStatus;
import ama.awesomemodeling.repositories.ControlRepository;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

@RequestMapping("/q1")
@RestController
public class QOneController {
    private final static String QUEUE_NAME = "q1";

    @Autowired
    private ControlRepository repo;

    @PostMapping("")
    String post(@RequestBody QOneCreateDTO dto) {

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
            System.out.println(" [x] Sent '" + saved.getId()+ "'");
        } catch (Exception e) {
            System.out.println(e);
        }

        return "Starting document retrieval";
    }

    public List<Hit<Article>> search(ElasticsearchClient esClient, String index, String query) throws IOException {

        SearchResponse<Article> response = esClient.search(s -> s
            .index(index)
            .query(q -> q
                .queryString(t -> t
                    .query(query)
                )
            ),
            Article.class
        );

        System.out.println("Searching done");
        return response.hits().hits();
    }

    @GetMapping("/{collectionId}/q2")
    String get(@PathVariable(value = "collectionId") String id, @RequestParam String query, @RequestParam int k) {
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
        try {
            List<Hit<Article>> hits = search(esClient, "articles_" + id, query);
            if (hits.isEmpty()) {
                System.out.println("No articles found");
            } else {
                for (Hit<Article> hit : hits) {
                    Article article = hit.source();
                    System.out.println("Found article: " + article.getTitle());
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

        return id + " " + query + " " + k;
    }
    
}
