package com.cooperativismo.Cooperativismo.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Voto {

    private static List<String> votosValidosString = Arrays.asList("SIM", "NÃO");
    long pautaId;
    String voto;
    Usuario usuario;

    /**
     * Verifica se as informações do voto esta válidos
     * @return  true se esta valido, falso se não tiver
     */
    public boolean votoValido(){
        return votosValidosString.contains(this.voto);
    }
}
