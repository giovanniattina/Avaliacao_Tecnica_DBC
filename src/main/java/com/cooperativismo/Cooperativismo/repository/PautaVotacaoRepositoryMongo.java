package com.cooperativismo.Cooperativismo.repository;

import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("PautaVotacaoRepository")
public interface PautaVotacaoRepositoryMongo extends MongoRepository<PautaVotacao, Long> {

    public Optional<PautaVotacao> findByPautaId(long id);
}
