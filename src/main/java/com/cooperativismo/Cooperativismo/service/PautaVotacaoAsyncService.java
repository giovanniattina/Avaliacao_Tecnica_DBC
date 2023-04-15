package com.cooperativismo.Cooperativismo.service;


import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Voto;
import com.cooperativismo.Cooperativismo.repository.PautaVotacaoRepositoryMongo;
import com.cooperativismo.Cooperativismo.producer.PautaVotacaoResultadoProducer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.slf4j.LoggerFactory.getLogger;

@Service
/**
 * Service para rodar tarefas asyns com a pauta de votação e não criar dependência ciclica com a PautaVotacaoAsynService
 * na dependencia de atualizar a pauta
 */
public class PautaVotacaoAsyncService {

    private final PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo;

    private final PautaVotacaoResultadoProducer pautaVotacaoResultadoProducer;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;


    private static Logger logger = getLogger(PautaVotacaoAsyncService.class);

    @Autowired
    public PautaVotacaoAsyncService(
            PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo,
            PautaVotacaoResultadoProducer pautaVotacaoResultadoProducer, ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.pautaVotacaoRepositoryMongo = pautaVotacaoRepositoryMongo;
        this.pautaVotacaoResultadoProducer = pautaVotacaoResultadoProducer;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    public void fecharVotacaoPautaAsync(PautaVotacao pautaVotacao){
        LocalDateTime horarioAbertura = pautaVotacao.getDataAbertura();
        LocalDateTime horarioFechamento = horarioAbertura.plusMinutes(pautaVotacao.getDuracaoMinutos());
        Instant instantFechamentoVotacao = horarioFechamento.atZone(ZoneId.systemDefault()).toInstant();

        logger.info(String.format(
                "Agendando fechando Sessão de Votação da Puata %s, aberta as %s, com duração de %s minutos",
                pautaVotacao.getPautaId(), pautaVotacao.getDataAbertura(), pautaVotacao.getDuracaoMinutos()
        ));
        threadPoolTaskScheduler.schedule(
                fecharVotacaoPautaAsyncTask(pautaVotacao),
                instantFechamentoVotacao
        );
    }

    private Runnable fecharVotacaoPautaAsyncTask(PautaVotacao pautaVotacao){
        return new Runnable() {
            @Override
            public void run() {
                logger.info(String.format(
                        "Fechando Sessão de Votação da Puata %s, aberta as %s, com duração de %s minutos",
                        pautaVotacao.getPautaId(), pautaVotacao.getDataAbertura(), pautaVotacao.getDuracaoMinutos()
                ));
                fecharVotacaoSessao(pautaVotacao);

                // após fechar pauta votação, publicar resultado em uma fila para outros serviços consumirem
                PautaSessaoVotacaoResultado pautaSessaoVotacaoResultado = resultadoVotacao(pautaVotacao.getPautaId());
                pautaVotacaoResultadoProducer.publicarResultadoVotacao(pautaSessaoVotacaoResultado);
            }
        };
    }
    private void fecharVotacaoSessao(PautaVotacao pautaVotacao){
        //Consultar a ultima versão da pauta no banco de dados por causa da concorrencia entre a votação e o fechamento da puata async
        PautaVotacao pautaVotacaoUpdated = pautaVotacaoRepositoryMongo
                .findByPautaId(pautaVotacao.getPautaId()).get();

        pautaVotacaoUpdated.setStatus("FECHADA");
        pautaVotacaoRepositoryMongo.save(pautaVotacaoUpdated);
    }

    private PautaSessaoVotacaoResultado resultadoVotacao(long pautaId) {

        PautaVotacao pautaVotacaoUpdated = pautaVotacaoRepositoryMongo
                .findByPautaId(pautaId).get();
        return consolidarPautaSessaoVotacaoResultado(pautaVotacaoUpdated);

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
