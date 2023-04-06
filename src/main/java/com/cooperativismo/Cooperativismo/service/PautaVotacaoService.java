package com.cooperativismo.Cooperativismo.service;

import com.cooperativismo.Cooperativismo.expection.*;
import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Usuario;
import com.cooperativismo.Cooperativismo.model.Voto;
import com.cooperativismo.Cooperativismo.repository.PautaVotacaoRepositoryMongo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PautaVotacaoService {

    private final  SequenceGeneratorService sequenceGeneratorService;
    private final PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo;
    private static Logger logger = getLogger(PautaVotacaoService.class);

    @Autowired
    public PautaVotacaoService(
            SequenceGeneratorService sequenceGeneratorService,
            @Qualifier("PautaVotacaoRepository") PautaVotacaoRepositoryMongo pautaVotacaoRepositoryMongo
    ){
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.pautaVotacaoRepositoryMongo = pautaVotacaoRepositoryMongo;
    }

    private boolean verificarSeVotacaoEstaAberta(PautaVotacao pautaVotacao){
        //true -> esta aberta, false fechada
        boolean returnValor = true;
        if(pautaVotacao.getStatus().equals("ABERTA")){
            LocalDateTime horarioAgora = LocalDateTime.now();
            LocalDateTime horarioAbertura = pautaVotacao.getDataAbertura();
            long duracao = pautaVotacao.getDuracaoMinutos();
            LocalDateTime horarioFechamento = horarioAbertura.plusMinutes(duracao);

            logger.info(String.format("horario agora = '%s', horario abertura = '%s', duração= '%s'", horarioAgora, horarioAbertura, duracao));

            if (horarioAgora.isAfter(horarioFechamento)){
                returnValor =  false;
            }else{
                logger.info("Ainda continua aberta");
            }
        }else{
            returnValor = false;
        }
        return returnValor;
    }

    private PautaVotacao criarPautaVocacao(long pautaId, int duracao){
        PautaVotacao pautaVotacao = new PautaVotacao();
        pautaVotacao.set_id(sequenceGeneratorService.generateSequence(pautaVotacao.SEQUENCE_NAME));
        pautaVotacao.setPautaId(pautaId);
        pautaVotacao.setDuracaoMinutos(duracao);
        pautaVotacao.setStatus("ABERTA");
        pautaVotacao.setDataAbertura(LocalDateTime.now());
        pautaVotacao.setVotos(new ArrayList<>());

        if(pautaVotacao.getDuracaoMinutos() == 0){
            pautaVotacao.setDuracaoMinutos(1);
        }
        logger.info(String.format("criando sessão: %s", pautaVotacao.toString()));

        pautaVotacaoRepositoryMongo.insert(pautaVotacao);
        return pautaVotacao;
    }
    public PautaVotacao abrirVotacao(long pautaId, int duracao) throws PautaVotacaoJaAbertaException {
        Optional<PautaVotacao> optionalPautaVotacao = pautaVotacaoRepositoryMongo.findByPautaId(pautaId);
        PautaVotacao pautaVotacao;
        if (optionalPautaVotacao.isEmpty()){
            logger.info("Abrir sessão");

            pautaVotacao = criarPautaVocacao(pautaId, duracao);

        }else{
            logger.info("Sessão já Aberta");
            pautaVotacao = optionalPautaVotacao.get();
            pautaVotacao = atualizarStatus(pautaVotacao); //atualizar status da pauta causa ja estajá fecada

            throw new PautaVotacaoJaAbertaException(
                    String.format(
                            "Votação da pauta %s foi aberta as %s e esta %s",
                            pautaVotacao.getPautaId(),
                            pautaVotacao.getDataAbertura(),
                            pautaVotacao.getStatus()
                    )
            );
        }

        return pautaVotacao;
    }

    private PautaVotacao atualizarStatus(PautaVotacao pautaVotacao){
        if(!verificarSeVotacaoEstaAberta(pautaVotacao)){
            //PautaVotacao pautaVotacaoPorId = pautaVotacaoRepositoryMongo.findById(pautaVotacao.get_id()).get();
            pautaVotacao.setStatus("FECHADA");
            pautaVotacaoRepositoryMongo.save(pautaVotacao);
        }

        return pautaVotacao;
    }

    public List<PautaVotacao> listarTodas() {
        return pautaVotacaoRepositoryMongo.findAll();
    }

    public void registarVoto(long pautaId, String votoTexto, Usuario usuario)
            throws SessaoNaoExisteException, SessaoFechadaExpection, UsuarioJaVotoException {

        logger.info(String.format("Voto em [pautaId='%s', votoTexto='%s', usuario='%s]", pautaId, votoTexto, usuario.toString()));
        //check se sessão existe
        Optional<PautaVotacao> optionalPautaVotacao = pautaVotacaoRepositoryMongo.findByPautaId(pautaId);
        if(optionalPautaVotacao.isEmpty()){
            throw new SessaoNaoExisteException(String.format("Sessao com id %s não existe", pautaId));
        }
        if(verificarSeVotacaoEstaAberta(optionalPautaVotacao.get())){            //check se sessao esta aberta
            logger.info("sessao aberta");


            Voto voto = new Voto(pautaId, votoTexto, usuario);
            PautaVotacao pautaVotacao = optionalPautaVotacao.get();
            List<Voto> votos = pautaVotacao.getVotos();
            if(!verificarSeUsuarioJaVoto(voto, votos)){
                votos.add(voto);
                pautaVotacao.setVotos(votos);

                pautaVotacaoRepositoryMongo.save(pautaVotacao);
            }else{
                throw new UsuarioJaVotoException(String.format("Usuario %s, já votou na pauta %s", usuario.toString(), pautaId));
            }


        }else{//avisar que sessao esta fechada e nao recebe mais votos
            throw new SessaoFechadaExpection(String.format("Sessao com id %s esta fechada", pautaId));
        }
    }

    private boolean verificarSeUsuarioJaVoto(Voto novoVoto, List<Voto> votos){
        //true -> ja voto , false -> não voto
        Optional<Voto> votoJaExiste=  votos.
                stream().
                filter(v -> {
                        String usuarioId =  v.getUsuario().getId();
                        return usuarioId.equals(novoVoto.getUsuario().getId());
                }).
                findFirst();
        return votoJaExiste.isPresent();
    }

    public PautaSessaoVotacaoResultado resultadoVotacao(long pautaId)
            throws SessaoNaoExisteException, SessaoVotacaoAindaAbertaExpection {
        Optional<PautaVotacao> optionalPautaVotacao = pautaVotacaoRepositoryMongo.findByPautaId(pautaId);

        //Verifica se pauta da votacao existe
        if (optionalPautaVotacao.isEmpty()){
            logger.warn(String.format("votação na pauta %s não existe", pautaId));
            throw new SessaoNaoExisteException(String.format("Sessao de votacao na pauta com  id %s não existe", pautaId));
        }

        PautaVotacao pautaVotacao = optionalPautaVotacao.get();
        //Verifica se pauta ainda esta aberta, caso sim, não retorna resultado, até esta fechada
        //Esse é um caso de uso tomado na decisao da criaçao do projeto
        pautaVotacao = atualizarStatus(pautaVotacao);//Antes de verificar status atualiza status

        if(pautaVotacao.getStatus().equals("ABERTA")){
            throw new SessaoVotacaoAindaAbertaExpection(
                    String.format(
                            "Pauta de votaçao com id %s ainda esta Aberta para votacao",
                            pautaVotacao.getPautaId()));
        }


        PautaSessaoVotacaoResultado resultado = consolidarPautaSessaoVotacaoResultado(pautaVotacao);
        logger.info(resultado.getResultado());
        return  resultado;

    }

    private PautaSessaoVotacaoResultado consolidarPautaSessaoVotacaoResultado(PautaVotacao pautaVotacao){
        int qntVotosSim = 0;
        int qntVotosNao = 0;
        //contabilizar votos
        for (Voto voto: pautaVotacao.getVotos()){
            if(voto.getVoto().equals("SIM")){
                qntVotosSim++;
            }else if(voto.getVoto().equals("NÃO")){
                qntVotosNao++;
            }
        }
        //verificar campeao
        String campeao = "";
        if(qntVotosSim > qntVotosNao){
            campeao = "SIM";
        }else if (qntVotosNao > qntVotosSim){
            campeao = "NÃO";
        }else{
            campeao = "EMPATE";
        }

        return new PautaSessaoVotacaoResultado(
                pautaVotacao.getPautaId(),
                qntVotosSim,
                qntVotosNao,
                campeao);
    }
}
