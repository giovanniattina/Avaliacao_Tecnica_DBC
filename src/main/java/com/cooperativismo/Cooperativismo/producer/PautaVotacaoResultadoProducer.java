package com.cooperativismo.Cooperativismo.producer;


import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

@Component
/**
 * Producer para enviar mensagens a um broker de menssageria (Kafka) do resultado de votação de uma sessão
 */
public class PautaVotacaoResultadoProducer {
    @Value(value = "${kafka.resultadoVotacaoPauta.topicName}")
    private String topicName;

    KafkaTemplate<String, PautaSessaoVotacaoResultado> kafkaTemplate;

    private static Logger logger = getLogger(PautaVotacaoResultadoProducer.class);


    public PautaVotacaoResultadoProducer(
            KafkaTemplate<String, PautaSessaoVotacaoResultado> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarResultadoVotacao(PautaSessaoVotacaoResultado pautaSessaoVotacaoResultado){
        logger.info(String.format(
                "Publicando resultado da votação da pauta %s no topico %s",
                pautaSessaoVotacaoResultado.toString(), topicName));

        CompletableFuture<SendResult<String, PautaSessaoVotacaoResultado>> future =
                kafkaTemplate.send(topicName, pautaSessaoVotacaoResultado);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info(String.format(
                        "Mensagem enviada %s with offset=[%s]",
                        pautaSessaoVotacaoResultado.toString(), result.getRecordMetadata().offset()
                ));
            } else {
                logger.warn(String.format(
                        "Mensagem não enviada, dado o erro: ",
                        pautaSessaoVotacaoResultado.toString(), ex.getMessage()
                ));
            }
        });
    }
}
