package com.cooperativismo.Cooperativismo.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AbrirSessaoRequest {

    long pautaId;

    int duracao;
}
