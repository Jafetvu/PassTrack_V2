package com.passtrack.api.dto.asistencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RegistroAsistenciaBulkRecord(
    @NotNull(message = "La fecha del pase de lista es obligatoria")
    LocalDate fecha,

    @NotEmpty(message = "La lista de asistencias no puede estar vacía")
    List<@NotNull @Valid PaseListaItemRecord> lista
) {
    public RegistroAsistenciaBulkRecord {
        lista = lista == null ? List.of() : List.copyOf(lista);
    }
}
