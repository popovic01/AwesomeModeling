package ama.awesomemodeling;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestRepository extends MongoRepository<Person, Integer> {
    
}
