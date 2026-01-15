package com.lab.reagentdispenser.controller;

import com.lab.reagentdispenser.dto.DispenseOperationDTO;
import com.lab.reagentdispenser.dto.DispenseRequestDTO;
import com.lab.reagentdispenser.service.DispenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/dispense")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class DispenseController {

	private final DispenseService dispenseService;

	@GetMapping("/history")
	public ResponseEntity<List<DispenseOperationDTO>> getOperationHistory() {
		log.info("GET /api/dispense/history - Get all operations");
		List<DispenseOperationDTO> operations = dispenseService.getAllOperations();
		return ResponseEntity.ok(operations);
	}

	@GetMapping("/{id}")
	public ResponseEntity<DispenseOperationDTO> getOperationById(@PathVariable Long id) {
		log.info("GET /api/dispense/{} - Get operation by id", id);
		DispenseOperationDTO operation = dispenseService.getOperationById(id);
		return ResponseEntity.ok(operation);
	}

	@GetMapping("/status/{status}")
	public ResponseEntity<List<DispenseOperationDTO>> getOperationsByStatus(@PathVariable String status) {
		log.info("GET /api/dispense/status/{} - Get operations by status", status);
		List<DispenseOperationDTO> operations = dispenseService.getOperationsByStatus(status);
		return ResponseEntity.ok(operations);
	}

	@PostMapping
	public ResponseEntity<DispenseOperationDTO> createOperation(@Valid @RequestBody DispenseRequestDTO request) {
		log.info("POST /api/dispense - Create new dispense operation");
		DispenseOperationDTO operation = dispenseService.createOperation(request);
		
		// Execute operation asynchronously
		CompletableFuture.runAsync(() -> {
			try {
				dispenseService.executeOperation(operation.getId());
			} catch (Exception e) {
				log.error("Error executing operation asynchronously", e);
			}
		});
		
		return ResponseEntity.status(HttpStatus.CREATED).body(operation);
	}

	@PostMapping("/{id}/execute")
	public ResponseEntity<DispenseOperationDTO> executeOperation(@PathVariable Long id) {
		log.info("POST /api/dispense/{}/execute - Execute operation", id);
		DispenseOperationDTO operation = dispenseService.executeOperation(id);
		return ResponseEntity.ok(operation);
	}
}
