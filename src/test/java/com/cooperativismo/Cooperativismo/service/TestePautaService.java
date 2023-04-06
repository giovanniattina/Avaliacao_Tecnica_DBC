package com.cooperativismo.Cooperativismo.service;


import com.cooperativismo.Cooperativismo.model.Pauta;
import com.cooperativismo.Cooperativismo.expection.PautaNaoExisteException;
import com.cooperativismo.Cooperativismo.repository.PautaRepositoryMongo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestePautaService {


    @Mock
    PautaRepositoryMongo pautaRepository;

    @Mock
    SequenceGeneratorService sequenceGeneratorService;


    @InjectMocks
    PautaService pautaService;


    @Test
    public void testeCriarNovaPauta(){
        String pautaName = "cachorros no hall";
        long id = 1L;
        Pauta novaPauta = new Pauta(id, pautaName);

        when(pautaRepository.insert(any(Pauta.class))).thenReturn(novaPauta);
        when(sequenceGeneratorService.generateSequence(any(String.class))).thenReturn(id);

        // Act

        Pauta pauta = pautaService.createPauta(pautaName);
        assertEquals(pauta, novaPauta);

        // Assert
        verify(sequenceGeneratorService, times(1)).generateSequence(Pauta.SEQUENCE_NAME);
        verify(pautaRepository, times(1)).insert(any(Pauta.class));

    }

    @Test
    public void listarTodasPautasTest() {
        // Arrange
        List<Pauta> expectedPautas = new ArrayList<>();
        expectedPautas.add(new Pauta(1L, "pauta 1"));
        expectedPautas.add(new Pauta(2L, "pauta 2"));
        when(pautaRepository.findAll()).thenReturn(expectedPautas);

        // Act
        List<Pauta> pautas = pautaService.listarTodasPautas();

        // Assert
        assertEquals(expectedPautas, pautas);
        verify(pautaRepository, times(1)).findAll();
    }

    @Test
    public void abrirVotacao_whenPautaExiste() throws PautaNaoExisteException {
        // Arrange

        long pautaId = 1L;
        String pautaName = "cachorros no hall";

        Pauta pauta = new Pauta(pautaId, pautaName);
        when(pautaRepository.findById(any(Long.class))).thenReturn(Optional.of(pauta));
        // Act

        Pauta pautaReturned = pautaService.buscarPautaPorId(pautaId);
        // Assert

        assertEquals(pauta, pautaReturned);
        verify(pautaRepository, times(1)).findById(any(Long.class));
    }
    @Test
    public void abrirVotacao_whenPautaNaoExiste_thenTrowsPautaNaoExisteException(){
        long pautaId = 1L;

        // Act

        when(pautaRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(null));

        // Assert

        assertThrows(PautaNaoExisteException.class, () -> pautaService.buscarPautaPorId(pautaId));
        verify(pautaRepository, times(1)).findById(any(Long.class));


    }

}
