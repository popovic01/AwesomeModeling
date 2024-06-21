package ama.awesomemodeling.entities;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QTwoTopics {
    @JsonProperty("topics")
    private ArrayList<ArrayList<String>> topics;

    public ArrayList<ArrayList<String>> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<ArrayList<String>> topics) {
        this.topics = topics;
    }

    public QTwoTopics(ArrayList<ArrayList<String>> topics) {
        this.topics = topics;
    }
}
