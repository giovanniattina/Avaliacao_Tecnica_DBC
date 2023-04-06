package com.cooperativismo.Cooperativismo;

import com.cooperativismo.Cooperativismo.model.Usuario;
import com.cooperativismo.Cooperativismo.model.Voto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class VotoModelTests {

    @Test
    public void validarVoto_whenVotoEstaValido_returnTrue(){
        long pautaId = 1;
        Usuario usuario = new Usuario();
        Voto voto = new Voto(pautaId, "SIM", usuario);
        Voto voto1 = new Voto(pautaId, "NÃO", usuario);

        // assert
        assertEquals(true, voto.votoValido());
        assertEquals(true, voto1.votoValido());

    }

    @Test
    public void validarVoto_whenVotoNãoEstaValido_returnFalse(){
        long pautaId = 1;
        Usuario usuario = new Usuario();
        Voto voto = new Voto(pautaId, "Sim", usuario);
        Voto voto1 = new Voto(pautaId, "NAO", usuario);

        // assert
        assertEquals(false, voto.votoValido());
        assertEquals(false, voto1.votoValido());
    }
}
