package com.ilu.system.operator.controller;
import com.ilu.system.operator.dto.CreateOperatorRequest;
import com.ilu.system.operator.dto.FormationDetailsDto;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.FormationAssignment;
import com.ilu.system.operator.service.OperatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/operators")
public class OperatorController {
    private final OperatorService operatorService;
    public OperatorController(OperatorService operatorService) { this.operatorService = operatorService; }
    @PostMapping
    public ResponseEntity<Operator> createOperator(@RequestBody CreateOperatorRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.createOperator(request)); }
    @PostMapping("/batch")
    public ResponseEntity<List<Operator>> createOperatorsBatch(@RequestBody List<CreateOperatorRequest> requests) { return ResponseEntity.status(HttpStatus.CREATED).body(operatorService.createOperatorsBatch(requests)); }
    @GetMapping
    public ResponseEntity<List<Operator>> listAll() { return ResponseEntity.ok(operatorService.listAll()); }
    @GetMapping("/active")
    public ResponseEntity<List<Operator>> listActive() { return ResponseEntity.ok(operatorService.listActive()); }
    @GetMapping("/{id}")
    public ResponseEntity<Operator> getById(@PathVariable Long id) { return ResponseEntity.ok(operatorService.findById(id)); }
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Operator> getByEmployeeId(@PathVariable String employeeId) { return ResponseEntity.ok(operatorService.findByEmployeeId(employeeId)); }
    @PutMapping("/{id}")
    public ResponseEntity<Operator> update(@PathVariable Long id, @RequestBody CreateOperatorRequest request) { return ResponseEntity.ok(operatorService.updateOperator(id, request)); }
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) { operatorService.deactivateOperator(id); return ResponseEntity.ok().build(); }
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) { operatorService.activateOperator(id); return ResponseEntity.ok().build(); }
    @GetMapping("/{id}/formations")
    public ResponseEntity<List<FormationDetailsDto>> getFormations(@PathVariable Long id) { return ResponseEntity.ok(operatorService.getOperatorFormations(id)); }
    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<FormationAssignment>> getAssignments(@PathVariable Long id) { return ResponseEntity.ok(operatorService.getOperatorAssignments(id)); }
}