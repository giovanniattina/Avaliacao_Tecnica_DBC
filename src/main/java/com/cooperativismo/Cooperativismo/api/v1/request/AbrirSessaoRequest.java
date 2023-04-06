package com.cooperativismo.Cooperativismo.api.v1.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AbrirSessaoRequest {

    @NotBlank
    long pautaId;

    int duracao;
}
