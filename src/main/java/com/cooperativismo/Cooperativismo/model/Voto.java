package com.cooperativismo.Cooperativismo.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Voto {
    long pautaId;
    String voto;
    Usuario usuario;
}
