package ama.awesomemodeling.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("control")
public class QOne {
    @Id
    String id;

    String topic;

    QOneStatus status;

    LocalDateTime submitted_time;
    LocalDateTime finished_time;
    
    LocalDate local_start_date;
    LocalDate local_end_date;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public QOneStatus getStatus() {
        return status;
    }
    public void setStatus(QOneStatus status) {  
        this.status = status;
    }
    public LocalDateTime getSubmitted_time() {
        return submitted_time;
    }
    public void setSubmitted_time(LocalDateTime submitted_time) {
        this.submitted_time = submitted_time;
    }
    public LocalDateTime getFinished_time() {
        return finished_time;
    }
    public void setFinished_time(LocalDateTime finished_time) {
        this.finished_time = finished_time;
    }
    public LocalDate getLocal_start_date() {
        return local_start_date;
    }
    public void setLocal_start_date(LocalDate local_start_date) {
        this.local_start_date = local_start_date;
    }
    public LocalDate getLocal_end_date() {
        return local_end_date;
    }
    public void setLocal_end_date(LocalDate local_end_date) {
        this.local_end_date = local_end_date;
    }
}
