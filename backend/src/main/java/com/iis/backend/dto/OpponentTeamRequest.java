package com.iis.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OpponentTeamRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @Min(0) @Max(99) Integer wins,
        @NotNull @Min(0) @Max(99) Integer losses,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 120) String coach,
        @Size(max = 500) String playStyle,
        @Size(max = 1000) String note,
        String teamType,
        @Valid List<OpponentPlayerRequest> players) {
}
