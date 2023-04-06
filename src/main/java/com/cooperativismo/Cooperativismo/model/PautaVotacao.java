package com.cooperativismo.Cooperativismo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document("PautaVotacao")
@Getter
@Setter
public class PautaVotacao {

    @Transient
    public static final String SEQUENCE_NAME = "users_sequence";
    @Id
    private long _id;
    long pautaId;

    LocalDateTime dataAbertura;
    int duracaoMinutos;//duração em minutos que a pauta pode fica aberta

    String status; //open and close

    List<Voto> votos;

    public String toString(){
        return String.format(
                "PautaVotacao[id=%s, pautaId='%s', dataAbertura='%s', duracaoMinutos='%s', status='%s']",
                _id, pautaId, dataAbertura, duracaoMinutos, status);

    }
}
