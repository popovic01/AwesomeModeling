package ama.awesomemodeling.entities;

import java.util.ArrayList;
import ama.awesomemodeling.entities.Topic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QTwoTopics {
    @JsonProperty("topics")
    private ArrayList<Topic> topics;

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<Topic> topics) {
        this.topics = topics;
    }

    public QTwoTopics(ArrayList<Topic> topics) {
        this.topics = topics;
    }
}
