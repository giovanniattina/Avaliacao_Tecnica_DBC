package com.cooperativismo.Cooperativismo.service;

import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Usuario;
import com.cooperativismo.Cooperativismo.model.Voto;
import com.cooperativismo.Cooperativismo.expection.PautaVotacaoJaAbertaException;
import com.cooperativismo.Cooperativismo.expection.SessaoNaoExisteException;
import com.cooperativismo.Cooperativismo.expection.SessaoVotacaoAindaAbertaExpection;
import com.cooperativismo.Cooperativismo.repository.PautaVotacaoRepositoryMongo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
public class TestePautaVotacaoService {

    @Mock
    PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo;
    @Mock
    SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    PautaVotacaoService pautaVotacaoService;

    //UNIT TEST nas regras para abrir votacao
    @Test
    public void abrirVotacao() throws PautaVotacaoJaAbertaException {
        long pautaId = 1L;
        int duracao = 60;
        long id = 1L;

        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(id);
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("ABERTA");

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.empty());
        when(sequenceGeneratorService.generateSequence(any(String.class))).thenReturn(id);
        when(pautaVotacaoRepositoryMongo.insert(any(PautaVotacao.class))).thenReturn(pautaVotacao);
        PautaVotacao pautaVotacaoExpect = pautaVotacaoService.abrirVotacao(pautaId, duracao);

        assertEquals(pautaVotacao.getPautaId(), pautaVotacaoExpect.getPautaId());
    }

    @Test
    public void abrirVotacao_whenDuracaoDefault() throws PautaVotacaoJaAbertaException {
        long pautaId = 1L;
        long id = 1L;
        int duracao = 0;

        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(id);
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setDuracaoMinutos(duracao);

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.empty());
        when(sequenceGeneratorService.generateSequence(any(String.class))).thenReturn(id);
        when(pautaVotacaoRepositoryMongo.insert(any(PautaVotacao.class))).thenReturn(pautaVotacao);
        PautaVotacao pautaVotacaoExpect = pautaVotacaoService.abrirVotacao(pautaId, 0);

        assertEquals(1, pautaVotacaoExpect.getDuracaoMinutos());


    }

    @Test
    public void abrirVotacao_whenVotacaoJaAberta_thenThrowsPautaVotacaoJaAbertaException() {
        // Arrange
        long pautaId = 1L;
        int duracao = 60;

        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("ABERTA");
        pautaVotacao.setDuracaoMinutos(duracao);
        pautaVotacao.setDataAbertura(LocalDateTime.now());

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));

        // Act + Assert
        assertThrows(PautaVotacaoJaAbertaException.class, () -> pautaVotacaoService.abrirVotacao(pautaId, duracao));
    }

    //UNIT Teste para adicionar voto em uma sessao em uma pauta

    //UNIT TEST para contabilizar votacao de uma sessao em uma pauta
    @Test
    public void contabilzarResultadoVotacaoPauta_whenVotacaoFechada_then_retornaSucessoComSim()
            throws SessaoNaoExisteException, SessaoVotacaoAindaAbertaExpection {
        long pautaId = 1L;
        int duracao = 60;
        long id = 1L;

        Usuario usuario1 = new Usuario();
        usuario1.setId("1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("2");


        Voto voto1 = new Voto(pautaId, "SIM", usuario1);
        Voto voto2 = new Voto(pautaId, "SIM", usuario2);
        List<Voto> votoList = new ArrayList<>();
        votoList.add(voto1);
        votoList.add(voto2);


        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(id);
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("FECHADA");
        pautaVotacao.setVotos(votoList);

        //expect
        PautaSessaoVotacaoResultado expectPautaSessaoVotacaoResultado =
                new PautaSessaoVotacaoResultado(pautaId, 2, 0, "SIM");

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));

        PautaSessaoVotacaoResultado returnPautaSessaoVotacaoResultado = pautaVotacaoService.resultadoVotacao(pautaId);


        // Assert
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeNao(),
                returnPautaSessaoVotacaoResultado.getQuantidadeNao());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeSim(),
                returnPautaSessaoVotacaoResultado.getQuantidadeSim());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getResultado(),
                expectPautaSessaoVotacaoResultado.getResultado());
        verify(
                pautaVotacaoRepositoryMongo,
                times(1))
                .findByPautaId(anyLong());

    }

    @Test
    public void contabilzarResultadoVotacaoPauta_whenVotacaoFechada_then_retornaSucessoComNao()
            throws SessaoNaoExisteException, SessaoVotacaoAindaAbertaExpection {
        long pautaId = 1L;
        int duracao = 60;
        long id = 1L;

        Usuario usuario1 = new Usuario();
        usuario1.setId("1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("2");


        Voto voto1 = new Voto(pautaId, "NÃO", usuario1);
        Voto voto2 = new Voto(pautaId, "NÃO", usuario2);
        List<Voto> votoList = new ArrayList<>();
        votoList.add(voto1);
        votoList.add(voto2);


        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(id);
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("FECHADA");
        pautaVotacao.setVotos(votoList);

        //expect
        PautaSessaoVotacaoResultado expectPautaSessaoVotacaoResultado =
                new PautaSessaoVotacaoResultado(pautaId, 0, 2, "NÃO");

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));

        PautaSessaoVotacaoResultado returnPautaSessaoVotacaoResultado = pautaVotacaoService.resultadoVotacao(pautaId);


        // Assert
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeNao(),
                returnPautaSessaoVotacaoResultado.getQuantidadeNao());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeSim(),
                returnPautaSessaoVotacaoResultado.getQuantidadeSim());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getResultado(),
                expectPautaSessaoVotacaoResultado.getResultado());
        verify(
                pautaVotacaoRepositoryMongo,
                times(1))
                .findByPautaId(anyLong());

    }

    @Test
    public void contabilzarResultadoVotacaoPauta_whenVotacaoFechada_then_retornaSucessoEmpate()
            throws SessaoNaoExisteException, SessaoVotacaoAindaAbertaExpection {
        // Arrange

        long pautaId = 1L;
        long id = 1L;

        Usuario usuario1 = new Usuario();
        usuario1.setId("1");
        Usuario usuario2 = new Usuario();
        usuario2.setId("2");


        Voto voto1 = new Voto(pautaId, "NÃO", usuario1);
        Voto voto2 = new Voto(pautaId, "SIM", usuario2);
        List<Voto> votoList = new ArrayList<>();
        votoList.add(voto1);
        votoList.add(voto2);


        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(id);
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("FECHADA");
        pautaVotacao.setVotos(votoList);

        //expect
        PautaSessaoVotacaoResultado expectPautaSessaoVotacaoResultado =
                new PautaSessaoVotacaoResultado(pautaId, 1, 1, "EMPATE");

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));

        // Act
        PautaSessaoVotacaoResultado returnPautaSessaoVotacaoResultado = pautaVotacaoService.resultadoVotacao(pautaId);


        // Assert
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeNao(),
                returnPautaSessaoVotacaoResultado.getQuantidadeNao());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeSim(),
                returnPautaSessaoVotacaoResultado.getQuantidadeSim());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getResultado(),
                expectPautaSessaoVotacaoResultado.getResultado());
        verify(
                pautaVotacaoRepositoryMongo,
                times(1))
                .findByPautaId(anyLong());

    }

    @Test
    public void contabilzarResultadoVotacaoPauta_whenVotacaoNaoAberta_then_SessaoNãoExisteExpection() {
        // Arrange

        long pautaId = 1L;

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.ofNullable(null));

        //assert
        assertThrows(SessaoNaoExisteException.class, () -> pautaVotacaoService.resultadoVotacao(pautaId));

    }

    @Test
    public void contabilizarResultadoVotacaoPauta_whenVotacaoAindaAberta_then_SessaoVotacaoAindaAbertaExpection() {
        /**
         * Caso: Status da votacao esta aberta e horario_atual e ainda nãoo esta no tempo de fechamento
         * horario_atual before horario_abertura+duracao
         */
        // Arrange

        long pautaId = 1L;
        int duracao = 10;
        LocalDateTime horarioAbertura = LocalDateTime.now();

        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("ABERTA");
        pautaVotacao.setDuracaoMinutos(duracao);
        pautaVotacao.setDataAbertura(horarioAbertura);

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));

        // Act //assert
        assertThrows(SessaoVotacaoAindaAbertaExpection.class, () -> pautaVotacaoService.resultadoVotacao(pautaId));


    }
    @Test
    public void contabilizarResultadoVotacaoPauta_whenPautaFechadaSemVotacao_then_RetornaSucessoComEmpate()
            throws SessaoNaoExisteException, SessaoVotacaoAindaAbertaExpection{
        // Arrange

        long pautaId = 1L;
        long id = 1L;

        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(id);
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setStatus("FECHADA");
        pautaVotacao.setVotos(new ArrayList<>());

        //expect
        PautaSessaoVotacaoResultado expectPautaSessaoVotacaoResultado =
                new PautaSessaoVotacaoResultado(pautaId, 0, 0, "EMPATE");

        when(pautaVotacaoRepositoryMongo.findByPautaId(anyLong())).thenReturn(Optional.of(pautaVotacao));

        // Act
        PautaSessaoVotacaoResultado returnPautaSessaoVotacaoResultado = pautaVotacaoService.resultadoVotacao(pautaId);
        // Assert
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeNao(),
                returnPautaSessaoVotacaoResultado.getQuantidadeNao());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getQuantidadeSim(),
                returnPautaSessaoVotacaoResultado.getQuantidadeSim());
        assertEquals(
                expectPautaSessaoVotacaoResultado.getResultado(),
                expectPautaSessaoVotacaoResultado.getResultado());
        verify(
                pautaVotacaoRepositoryMongo,
                times(1))
                .findByPautaId(anyLong());


    }
}
