package com.cooperativismo.Cooperativismo.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Usuario {

    String id; // id do usuário é o CPF

    public String Usuario(){
        return String.format("Usuario[id=%s]", id);

    }
}
