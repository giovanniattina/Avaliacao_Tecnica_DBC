package com.cooperativismo.Cooperativismo.api.request;

import com.cooperativismo.Cooperativismo.model.Usuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioVotaSessaoRequest {

    long pautaId;
    String voto;
    Usuario usuario;
}
