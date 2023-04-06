package com.cooperativismo.Cooperativismo.service;

import com.cooperativismo.Cooperativismo.model.Pauta;
import com.cooperativismo.Cooperativismo.expection.PautaNaoExisteException;
import com.cooperativismo.Cooperativismo.repository.PautaRepositoryMongo;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PautaService {

    private final PautaRepositoryMongo pautaRepositoryMongo;
    private final  SequenceGeneratorService sequenceGeneratorService;
    private static Logger logger = getLogger(PautaVotacaoService.class);

    public PautaService(PautaRepositoryMongo pautaRepositoryMongo, SequenceGeneratorService sequenceGeneratorService) {
        this.pautaRepositoryMongo = pautaRepositoryMongo;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }


    public Pauta createPauta(String pautaNome){

        Pauta pauta = new Pauta(
                sequenceGeneratorService.generateSequence(Pauta.SEQUENCE_NAME),
                pautaNome
        );
        logger.info(String.format("Pauta %s criada", pauta.toString()));
        return pautaRepositoryMongo.insert(pauta);
    }

    public List<Pauta> listarTodasPautas(){
        return pautaRepositoryMongo.findAll();
    }

    public Pauta buscarPautaPorId(long pautaId) throws PautaNaoExisteException {
        Optional<Pauta> optionalPauta = pautaRepositoryMongo.findById(pautaId);
        if(optionalPauta.isEmpty()){
            throw new PautaNaoExisteException(String.format("Pauta com id %s não existe", pautaId));
        }
        return optionalPauta.get();
    }
}
