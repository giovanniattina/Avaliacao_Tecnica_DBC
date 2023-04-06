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
        logger.info(String.format("Sessão %s criada para pauta %s", pautaVotacao.toString(), pautaId));

        pautaVotacaoRepositoryMongo.insert(pautaVotacao);
        return pautaVotacao;
    }
    public PautaVotacao abrirVotacao(long pautaId, int duracao) throws PautaVotacaoJaAbertaException {
        Optional<PautaVotacao> optionalPautaVotacao = pautaVotacaoRepositoryMongo.findByPautaId(pautaId);
        PautaVotacao pautaVotacao;
        if (optionalPautaVotacao.isEmpty()){

            pautaVotacao = criarPautaVocacao(pautaId, duracao);

        }else{
            pautaVotacao = optionalPautaVotacao.get();
            pautaVotacao = atualizarStatus(pautaVotacao); //atualizar status da pauta causa ja está

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



    public List<PautaVotacao> listarTodas() {
        return pautaVotacaoRepositoryMongo.findAll();
    }

    public PautaVotacao registarVoto(long pautaId, String votoTexto, Usuario usuario)
            throws SessaoNaoExisteException, SessaoFechadaExpection, UsuarioJaVotoException, VotoInvalidoException {

        Voto voto = new Voto(pautaId, votoTexto, usuario);
        //Valida de voto esta certo
        if (!voto.votoValido()) throw new VotoInvalidoException("Voto invalido, possível valores são SIM e NÃO");


        logger.info(String.format("Voto em [pautaId='%s', votoTexto='%s', usuario='%s]", pautaId, votoTexto, usuario.toString()));

        //Verifica se sessão da puata existe para receber foto
        Optional<PautaVotacao> optionalPautaVotacao = pautaVotacaoRepositoryMongo.findByPautaId(pautaId);

        if(optionalPautaVotacao.isEmpty()){
            throw new SessaoNaoExisteException(String.format("Pauta com %d não tem Sessao para votação aberta", pautaId));
        }

        //Pauta ja existente
        PautaVotacao pautaVotacao = optionalPautaVotacao.get();

        if(verificarSeVotacaoEstaAberta(pautaVotacao)){            //check se sessao de votação esta aberta
            List<Voto> votos = pautaVotacao.getVotos();

            if(!verificarSeUsuarioJaVoto(voto, votos)){
                votos.add(voto);
                pautaVotacao.setVotos(votos);

                pautaVotacao = pautaVotacaoRepositoryMongo.save(pautaVotacao);
            }else{
                throw new UsuarioJaVotoException(String.format("Usuario %s, já votou na pauta %s", usuario.getId(), pautaId));
            }


        }else{//avisar que sessao de votação esta fechada e nao recebe mais votos
            atualizarStatus(pautaVotacao);
            throw new SessaoFechadaExpection(String.format("Sessao da Pauta com id %s esta fechada", pautaId));
        }
        atualizarStatus(pautaVotacao); // Atualizar a sessão sempre quem receber um voto
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
    private boolean verificarSeVotacaoEstaAberta(PautaVotacao pautaVotacao){
        //true -> Sessao esta aberta e recebo votos
        //false ->Sessao esta fechada e não recebe votos
        boolean returnValor = true;
        if(pautaVotacao.getStatus().equals("ABERTA")){
            //Se a sessao estiver como aberta, verificar se o tempo de duração já não expirou

            LocalDateTime horarioAgora = LocalDateTime.now();
            LocalDateTime horarioAbertura = pautaVotacao.getDataAbertura();
            long duracao = pautaVotacao.getDuracaoMinutos();

            LocalDateTime horarioFechamento = horarioAbertura.plusMinutes(duracao);
            if (horarioAgora.isAfter(horarioFechamento)){ //se sessao passou do horário de duração, retorna par fechar ela
                returnValor =  false;
            }
        }else{
            returnValor = false;
        }
        return returnValor;
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
