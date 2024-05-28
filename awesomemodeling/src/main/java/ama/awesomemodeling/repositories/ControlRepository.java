package ama.awesomemodeling.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import ama.awesomemodeling.entities.QOne;

public interface ControlRepository extends MongoRepository<QOne, String>{
    
}
