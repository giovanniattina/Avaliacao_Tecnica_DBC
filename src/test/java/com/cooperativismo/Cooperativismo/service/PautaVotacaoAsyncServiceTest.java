package com.cooperativismo.Cooperativismo.service;


import com.cooperativismo.Cooperativismo.config.ThreadPoolTaskSchedulerConfig;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Voto;
import com.cooperativismo.Cooperativismo.producer.PautaVotacaoResultadoProducer;
import com.cooperativismo.Cooperativismo.repository.PautaVotacaoRepositoryMongo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PautaVotacaoAsyncServiceTest {

    @Mock
    PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo;

    @Mock
    PautaVotacaoResultadoProducer pautaVotacaoResultadoProducer;

    PautaVotacaoAsyncService pautaVotacaoAsyncService;

    private ThreadPoolTaskScheduler threadPoolTaskScheduler;


    @Before
    public void setUp(){
        threadPoolTaskScheduler = new ThreadPoolTaskSchedulerConfig().threadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        pautaVotacaoAsyncService = new PautaVotacaoAsyncService(
                pautaVotacaoRepositoryMongo,
                pautaVotacaoResultadoProducer,
                threadPoolTaskScheduler);
    }

    @Test
    public void fecharVotacao_testaChamadaAsync() throws InterruptedException {
        // Esse test é apenas para testar até a chamada async, não executa ela
        PautaVotacao pautaVotacao = new PautaVotacao();
        int duracao = 0;
        LocalDateTime horarioDeAbertura = LocalDateTime.now().minusMinutes(duracao);


        pautaVotacao.setStatus("ABERTA");
        pautaVotacao.setDataAbertura(horarioDeAbertura);
        pautaVotacao.setDuracaoMinutos(duracao);
        pautaVotacao.setVotos(new ArrayList<Voto>());

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));
        when(pautaVotacaoRepositoryMongo.save(any(PautaVotacao.class))).thenReturn(pautaVotacao);

        pautaVotacaoAsyncService.fecharVotacaoPautaAsync(pautaVotacao);

        verify(pautaVotacaoRepositoryMongo, times(1)).findByPautaId(anyLong());
        verify(pautaVotacaoRepositoryMongo, times(1)).save(any(PautaVotacao.class));
    }
}
