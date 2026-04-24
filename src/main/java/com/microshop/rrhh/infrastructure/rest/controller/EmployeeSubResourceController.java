package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.command.EmployeeSubResourceCommandService;
import com.microshop.rrhh.application.dto.employee.*;
import com.microshop.rrhh.application.query.EmployeeSubResourceQueryService;
import com.microshop.rrhh.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.EMPLOYEES)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class EmployeeSubResourceController {

    private final EmployeeSubResourceCommandService commandService;
    private final EmployeeSubResourceQueryService queryService;

    // ── Emergency Contacts ────────────────────────────────────────────────────

    @GetMapping("/{employeeId}/emergency-contacts")
    @Tag(name = "Emergency Contacts")
    @Operation(summary = "Listar contactos de emergencia de un empleado")
    public ResponseEntity<List<EmergencyContactDto.Response>> getEmergencyContacts(@PathVariable Long employeeId) {
        return ResponseEntity.ok(queryService.getEmergencyContacts(employeeId));
    }

    @PostMapping("/{employeeId}/emergency-contacts")
    @Tag(name = "Emergency Contacts")
    @Operation(summary = "Crear contacto de emergencia")
    public ResponseEntity<EmergencyContactDto.Response> createEmergencyContact(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmergencyContactDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.createEmergencyContact(employeeId, request));
    }

    @PutMapping("/{employeeId}/emergency-contacts/{id}")
    @Tag(name = "Emergency Contacts")
    @Operation(summary = "Actualizar contacto de emergencia")
    public ResponseEntity<EmergencyContactDto.Response> updateEmergencyContact(
            @PathVariable Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody EmergencyContactDto.Request request) {
        return ResponseEntity.ok(commandService.updateEmergencyContact(id, request));
    }

    @DeleteMapping("/{employeeId}/emergency-contacts/{id}")
    @Tag(name = "Emergency Contacts")
    @Operation(summary = "Eliminar contacto de emergencia")
    public ResponseEntity<Void> deleteEmergencyContact(@PathVariable Long employeeId, @PathVariable Long id) {
        commandService.deleteEmergencyContact(id);
        return ResponseEntity.noContent().build();
    }

    // ── Dependents ────────────────────────────────────────────────────────────

    @GetMapping("/{employeeId}/dependents")
    @Tag(name = "Dependents")
    @Operation(summary = "Listar dependientes de un empleado")
    public ResponseEntity<List<DependentDto.Response>> getDependents(@PathVariable Long employeeId) {
        return ResponseEntity.ok(queryService.getDependents(employeeId));
    }

    @PostMapping("/{employeeId}/dependents")
    @Tag(name = "Dependents")
    @Operation(summary = "Crear dependiente")
    public ResponseEntity<DependentDto.Response> createDependent(
            @PathVariable Long employeeId,
            @Valid @RequestBody DependentDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.createDependent(employeeId, request));
    }

    @PutMapping("/{employeeId}/dependents/{id}")
    @Tag(name = "Dependents")
    @Operation(summary = "Actualizar dependiente")
    public ResponseEntity<DependentDto.Response> updateDependent(
            @PathVariable Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody DependentDto.Request request) {
        return ResponseEntity.ok(commandService.updateDependent(id, request));
    }

    @DeleteMapping("/{employeeId}/dependents/{id}")
    @Tag(name = "Dependents")
    @Operation(summary = "Eliminar dependiente")
    public ResponseEntity<Void> deleteDependent(@PathVariable Long employeeId, @PathVariable Long id) {
        commandService.deleteDependent(id);
        return ResponseEntity.noContent().build();
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    @GetMapping("/{employeeId}/documents")
    @Tag(name = "Documents")
    @Operation(summary = "Listar documentos de un empleado")
    public ResponseEntity<List<DocumentDto.Response>> getDocuments(@PathVariable Long employeeId) {
        return ResponseEntity.ok(queryService.getDocuments(employeeId));
    }

    @PostMapping("/{employeeId}/documents")
    @Tag(name = "Documents")
    @Operation(summary = "Crear documento")
    public ResponseEntity<DocumentDto.Response> createDocument(
            @PathVariable Long employeeId,
            @Valid @RequestBody DocumentDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.createDocument(employeeId, request));
    }

    @PutMapping("/{employeeId}/documents/{id}")
    @Tag(name = "Documents")
    @Operation(summary = "Actualizar documento")
    public ResponseEntity<DocumentDto.Response> updateDocument(
            @PathVariable Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody DocumentDto.Request request) {
        return ResponseEntity.ok(commandService.updateDocument(id, request));
    }

    @DeleteMapping("/{employeeId}/documents/{id}")
    @Tag(name = "Documents")
    @Operation(summary = "Eliminar documento")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long employeeId, @PathVariable Long id) {
        commandService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // ── Salary History ────────────────────────────────────────────────────────

    @GetMapping("/{employeeId}/salary-history")
    @Tag(name = "Salary History")
    @Operation(summary = "Historial salarial de un empleado")
    public ResponseEntity<List<SalaryDto.Response>> getSalaryHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(queryService.getSalaryHistory(employeeId));
    }

    @PostMapping("/{employeeId}/salary-history")
    @Tag(name = "Salary History")
    @Operation(summary = "Agregar registro salarial")
    public ResponseEntity<SalaryDto.Response> createSalaryRecord(
            @PathVariable Long employeeId,
            @Valid @RequestBody SalaryDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandService.createSalaryRecord(employeeId, request));
    }
}
