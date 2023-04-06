package com.cooperativismo.Cooperativismo.expection;

public class PautaNaoExisteException extends Exception{
    public PautaNaoExisteException(String errorMessage) {
        super(errorMessage);
    }
}
