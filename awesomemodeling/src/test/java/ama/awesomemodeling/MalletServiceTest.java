package ama.awesomemodeling;

import static org.junit.jupiter.api.Assertions.*;

import ama.awesomemodeling.entities.Topic;
import ama.awesomemodeling.services.MalletService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class MalletServiceTest {
    private static MalletService malletService;

    @BeforeAll
    static void setUp() {
        malletService = new MalletService();
    }

    @Test
    void getTopicsTest() {
        ArrayList<String> articles = new ArrayList<>();
        // Example articles related to Formula 1
        articles.add("Lando Norris confident of cutting Verstappen’s Formula One lead");
        articles.add("Charles Leclerc wins Monaco F1 GP for Ferrari to delight of home crowd");

        int nTopics = 2;
        ArrayList<Topic> topics = malletService.getTopics(articles, nTopics);
        assertNotNull(topics);
        assertEquals(topics.size(), nTopics);
    }
}
