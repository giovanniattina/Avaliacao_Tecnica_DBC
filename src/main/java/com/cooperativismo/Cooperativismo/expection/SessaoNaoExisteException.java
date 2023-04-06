package com.cooperativismo.Cooperativismo.expection;

public class SessaoNaoExisteException extends Exception{
    public SessaoNaoExisteException(String errorMessage){
        super(errorMessage);
    }
}
