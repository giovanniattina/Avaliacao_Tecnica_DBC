package com.cooperativismo.Cooperativismo.api.v1;


import com.cooperativismo.Cooperativismo.api.v1.request.AbrirSessaoRequest;
import com.cooperativismo.Cooperativismo.api.v1.request.UsuarioVotaSessaoRequest;
import com.cooperativismo.Cooperativismo.expection.*;
import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Usuario;
import com.cooperativismo.Cooperativismo.service.PautaService;
import com.cooperativismo.Cooperativismo.service.PautaVotacaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequestMapping("api/v1/pautas")
@RestController
@Tag(name="PautaSessao", description = "Endpoints para abrir sessão, votar e ver resultado em uma Pauta")
public  class PautaSessaoV1Controller {

    private final PautaService pautaService;
    private final PautaVotacaoService pautaVotacaoService;

    @Autowired
    public PautaSessaoV1Controller(
            PautaService pautaService,
            PautaVotacaoService pautaVotacaoService){
        this.pautaService = pautaService;
        this.pautaVotacaoService = pautaVotacaoService;
    }
    @PostMapping(path = "sessao/abrir")
    public PautaVotacao abrirSessao(@RequestBody AbrirSessaoRequest abrirSessaoRequest){
        long pautaId = abrirSessaoRequest.getPautaId();
        PautaVotacao pautaVotacao = null;
        try {

            if(pautaService.buscarPautaPorId(pautaId) != null){
                pautaVotacao =  pautaVotacaoService.abrirVotacao(pautaId, abrirSessaoRequest.getDuracao());
            }
        }catch (PautaVotacaoJaAbertaException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }catch (PautaNaoExisteException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return pautaVotacao;
    }
    @PostMapping(path = "sessao/votar")
    public void votar(@RequestBody UsuarioVotaSessaoRequest usuarioVotaSessaoRequest)
            throws SessaoNaoExisteException, SessaoFechadaExpection, UsuarioJaVotoException {
        long sessaoPautaId = usuarioVotaSessaoRequest.getPautaId();
        String voto = usuarioVotaSessaoRequest.getVoto();
        Usuario usuario = usuarioVotaSessaoRequest.getUsuario();
        try{
            pautaVotacaoService.registarVoto(sessaoPautaId, voto, usuario);
        }catch (SessaoFechadaExpection | UsuarioJaVotoException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }catch (SessaoNaoExisteException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }catch (VotoInvalidoException e){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        }
    }

    @GetMapping(path = "sessao")
    public List<PautaVotacao> listarPautas(){
        return pautaVotacaoService.listarTodas();
    }

    @GetMapping(path = "sessao/resultado/{pautaId}")
    public PautaSessaoVotacaoResultado resultadoVotacao(@PathVariable("pautaId") long pautaId) throws SessaoNaoExisteException {
        try {
            return pautaVotacaoService.resultadoVotacao(pautaId);

        }catch (SessaoVotacaoAindaAbertaExpection e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SessaoNaoExisteException e ){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

}
