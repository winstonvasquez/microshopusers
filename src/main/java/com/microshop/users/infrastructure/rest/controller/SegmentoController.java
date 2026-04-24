package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.SegmentoCommandService;
import com.microshop.users.application.dto.SegmentoRequestDto;
import com.microshop.users.application.dto.SegmentoResponseDto;
import com.microshop.users.application.query.SegmentoQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.SEGMENTS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Segmentos", description = "Gestión de segmentos de clientes")
public class SegmentoController {

    private final SegmentoCommandService commandService;
    private final SegmentoQueryService queryService;

    @GetMapping
    @Operation(summary = "Listar segmentos activos (paginado)")
    public ResponseEntity<Page<SegmentoResponseDto>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET {} - search={}", ApiPaths.SEGMENTS, search);
        return ResponseEntity.ok(queryService.findAll(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener segmento por ID")
    public ResponseEntity<SegmentoResponseDto> getById(@PathVariable Long id) {
        log.info("GET {}/{}", ApiPaths.SEGMENTS, id);
        return ResponseEntity.ok(queryService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo segmento")
    public ResponseEntity<SegmentoResponseDto> create(@RequestBody @Valid SegmentoRequestDto dto) {
        log.info("POST {} - nombre={}", ApiPaths.SEGMENTS, dto.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar segmento existente")
    public ResponseEntity<SegmentoResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid SegmentoRequestDto dto) {
        log.info("PUT {}/{}", ApiPaths.SEGMENTS, id);
        return ResponseEntity.ok(commandService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar segmento (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE {}/{}", ApiPaths.SEGMENTS, id);
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
