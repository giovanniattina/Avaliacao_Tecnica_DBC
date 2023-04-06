package com.cooperativismo.Cooperativismo.api.v1.request;

import com.cooperativismo.Cooperativismo.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioVotaSessaoRequest {

    @NotBlank

    long pautaId;
    @NotBlank
    String voto;
    @NotBlank
    Usuario usuario;
}
