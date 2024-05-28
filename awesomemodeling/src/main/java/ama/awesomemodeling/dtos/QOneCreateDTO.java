package ama.awesomemodeling.dtos;
import java.time.LocalDate;

public class QOneCreateDTO {
    public String topic;
    public LocalDate local_start_date;
    public LocalDate local_end_date;

    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
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
