package ama.awesomemodeling.services;

import org.springframework.stereotype.Service;

import ama.awesomemodeling.entities.Article;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service("elastic")
public class ElasticService {
    private List<Hit<Article>> search(ElasticsearchClient esClient, String index, String field, String query)
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
    public ArrayList<String> retrieveDocuments (String id, String query) {
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
        return articleContents;
    }
}
