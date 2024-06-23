package ama.awesomemodeling;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ama.awesomemodeling.dtos.QOneCreateDTO;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ama.awesomemodeling.entities.QOne;
import ama.awesomemodeling.enums.QOneStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
@AutoConfigureMockMvc
class WebApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllSubjects() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/q1/")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addSubject() throws Exception {
        QOneCreateDTO f1Subject = new QOneCreateDTO();
        f1Subject.setTopic("Formula 1");
        f1Subject.setLocal_start_date(LocalDate.of(2024, 5, 1));
        f1Subject.setLocal_end_date(LocalDate.of(2024, 6, 1));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/q1")
                        .content(asJsonString(f1Subject))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }
}