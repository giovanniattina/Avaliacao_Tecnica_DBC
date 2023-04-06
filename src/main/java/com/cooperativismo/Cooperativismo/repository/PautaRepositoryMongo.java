package com.cooperativismo.Cooperativismo.repository;

import com.cooperativismo.Cooperativismo.model.Pauta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("PautaRepository")
public interface PautaRepositoryMongo extends MongoRepository<Pauta, Long> {

}
