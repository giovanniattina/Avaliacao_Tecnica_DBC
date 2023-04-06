package com.cooperativismo.Cooperativismo.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NovaPautaRequest {
    @NotBlank
    String name;
}
