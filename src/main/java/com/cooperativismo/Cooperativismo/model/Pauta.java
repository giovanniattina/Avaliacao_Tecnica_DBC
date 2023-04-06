package com.cooperativismo.Cooperativismo.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
@Document("Pauta")
@Getter
@Setter
@AllArgsConstructor
public class Pauta {
    @Transient
    public static final String SEQUENCE_NAME = "users_sequence";

    @Id
    private long id;

    private final String name;

    @Override
    public String toString(){
        return String.format(
                "Pauta[id=%s, name='%s']",
                id, name);

    }


}
