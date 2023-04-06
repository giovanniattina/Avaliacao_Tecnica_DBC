package com.cooperativismo.Cooperativismo.api.v1;
import com.cooperativismo.Cooperativismo.api.v1.request.AbrirSessaoRequest;
import com.cooperativismo.Cooperativismo.api.v1.request.NovaPautaRequest;
import com.cooperativismo.Cooperativismo.api.v1.request.UsuarioVotaSessaoRequest;
import com.cooperativismo.Cooperativismo.expection.*;
import com.cooperativismo.Cooperativismo.model.Pauta;
import com.cooperativismo.Cooperativismo.model.PautaSessaoVotacaoResultado;
import com.cooperativismo.Cooperativismo.model.PautaVotacao;
import com.cooperativismo.Cooperativismo.model.Usuario;
import com.cooperativismo.Cooperativismo.service.PautaService;
import com.cooperativismo.Cooperativismo.service.PautaVotacaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@RequestMapping("api/v1/pautas")
@RestController
@Tag(name="Pauta", description = "Endpoints para CRUD no objeto Pauta ")

public class PautaV1Controller {

    private final PautaService pautaService;
    private final PautaVotacaoService pautaVotacaoService;

    @Autowired
    public PautaV1Controller(
            PautaService pautaService,
            PautaVotacaoService pautaVotacaoService){
        this.pautaService = pautaService;
        this.pautaVotacaoService = pautaVotacaoService;
    }

    @PostMapping(path = "/create")
     public Pauta createPauta(@Valid @NotNull @RequestBody NovaPautaRequest pautaRequest){
        return pautaService.createPauta(pautaRequest.getName());
    }
    @GetMapping()
    public List<Pauta> list() {
        return pautaService.listarTodasPautas();
    }


}
