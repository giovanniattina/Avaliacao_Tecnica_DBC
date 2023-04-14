package com.cooperativismo.Cooperativismo.service;


import com.cooperativismo.Cooperativismo.expection.SessaoNaoExisteException;
import com.cooperativismo.Cooperativismo.expection.SessaoVotacaoAindaAbertaExpection;
import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Voto;
import com.cooperativismo.Cooperativismo.repository.PautaVotacaoRepositoryMongo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
/**
 * Service para rodar tarefas asyns com a pauta de votação e não criar dependência ciclica com a PautaVotacaoAsynService
 * na dependencia de atualizar a pauta
 */
public class PautaVotacaoAsyncService {

    private final PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo;

    private static Logger logger = getLogger(PautaVotacaoAsyncService.class);

    @Autowired
    public PautaVotacaoAsyncService(PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo) {
        this.pautaVotacaoRepositoryMongo = pautaVotacaoRepositoryMongo;
    }

    public Runnable fecharVotacaoPautaAsyncTask(PautaVotacao pautaVotacao){
        return new Runnable() {
            @Override
            public void run() {
                logger.info(String.format(
                        "Fechando Sessão de Votação da Puata %s, aberta as %s, com duração de %s minutos",
                        pautaVotacao.getPautaId(), pautaVotacao.getDataAbertura(), pautaVotacao.getDuracaoMinutos()
                ));
                fecharVotacaoSessao(pautaVotacao);

                PautaSessaoVotacaoResultado pautaSessaoVotacaoResultado = resultadoVotacao(pautaVotacao.getPautaId());
                logger.info(String.format("" +
                                "Pauta %s, Resultado: %s, Sim: %s, Não: %s",
                        pautaSessaoVotacaoResultado.getPautaId(),
                        pautaSessaoVotacaoResultado.getResultado(),
                        pautaSessaoVotacaoResultado.getQuantidadeSim(),
                        pautaSessaoVotacaoResultado.getQuantidadeNao()));
            }
        };
    }
    public void fecharVotacaoSessao(PautaVotacao pautaVotacao){
        PautaVotacao pautaVotacaoUpdated = pautaVotacaoRepositoryMongo
                .findByPautaId(pautaVotacao.getPautaId()).get();

        pautaVotacaoUpdated.setStatus("FECHADA");
        pautaVotacaoRepositoryMongo.save(pautaVotacaoUpdated);
    }

    public PautaSessaoVotacaoResultado resultadoVotacao(long pautaId) {

        PautaVotacao pautaVotacaoUpdated = pautaVotacaoRepositoryMongo
                .findByPautaId(pautaId).get();
        PautaSessaoVotacaoResultado resultado = consolidarPautaSessaoVotacaoResultado(pautaVotacaoUpdated);
        return  resultado;

    }

    private PautaSessaoVotacaoResultado consolidarPautaSessaoVotacaoResultado(PautaVotacao pautaVotacao){
        //TODO: mover essa mudança para outro local, porque precisa repetir ela aqui e na PautaVotacaoService
        int qntVotosSim = 0;
        int qntVotosNao = 0;
        //contabilizar votos
        for (Voto voto: pautaVotacao.getVotos()){
            if(voto.getVoto().equals("SIM")){
                qntVotosSim++;
            }else if(voto.getVoto().equals("NÃO")){
                qntVotosNao++;
            }
        }
        //verificar campeao
        String campeao = "";
        if(qntVotosSim > qntVotosNao){
            campeao = "SIM";
        }else if (qntVotosNao > qntVotosSim){
            campeao = "NÃO";
        }else{
            campeao = "EMPATE";
        }

        return new PautaSessaoVotacaoResultado(
                pautaVotacao.getPautaId(),
                qntVotosSim,
                qntVotosNao,
                campeao);
    }
}
