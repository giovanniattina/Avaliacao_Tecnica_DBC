package com.cooperativismo.Cooperativismo.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PautaSessaoVotacaoResultado {

    private long pautaId;
    private int quantidadeSim;
    private int quantidadeNao;

    private String resultado;

    public String toString(){
        return String.format(
                "PautaSessaoVotacaoResultado[pautaId=%s, quantidadeSim='%s', quantidadeNao='%s', resultado='%s']",
                pautaId, quantidadeSim, quantidadeNao, resultado);

    }
}
