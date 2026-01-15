package com.lab.reagentdispenser.controller;

import com.lab.reagentdispenser.dto.AddOperationToBatchRequestDTO;
import com.lab.reagentdispenser.dto.CreateBatchRequestDTO;
import com.lab.reagentdispenser.dto.DispenseBatchDTO;
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
		log.info("POST /api/dispense - Create new dispense operation (not executed immediately)");
		DispenseOperationDTO operation = dispenseService.createOperation(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(operation);
	}

	@PostMapping("/{id}/execute")
	public ResponseEntity<DispenseOperationDTO> executeOperation(@PathVariable Long id) {
		log.info("POST /api/dispense/{}/execute - Execute operation", id);
		DispenseOperationDTO operation = dispenseService.executeOperation(id);
		return ResponseEntity.ok(operation);
	}

	// Batch operations endpoints

	@PostMapping("/batch")
	public ResponseEntity<DispenseBatchDTO> createBatch(@Valid @RequestBody CreateBatchRequestDTO request) {
		log.info("POST /api/dispense/batch - Create new dispense batch");
		DispenseBatchDTO batch = dispenseService.createBatch(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(batch);
	}

	@GetMapping("/batch")
	public ResponseEntity<List<DispenseBatchDTO>> getAllBatches() {
		log.info("GET /api/dispense/batch - Get all batches");
		List<DispenseBatchDTO> batches = dispenseService.getAllBatches();
		return ResponseEntity.ok(batches);
	}

	@GetMapping("/batch/{id}")
	public ResponseEntity<DispenseBatchDTO> getBatchById(@PathVariable Long id) {
		log.info("GET /api/dispense/batch/{} - Get batch by id", id);
		DispenseBatchDTO batch = dispenseService.getBatchById(id);
		return ResponseEntity.ok(batch);
	}

	@PostMapping("/batch/{id}/add-operation")
	public ResponseEntity<DispenseBatchDTO> addOperationToBatch(
			@PathVariable Long id,
			@Valid @RequestBody AddOperationToBatchRequestDTO request) {
		log.info("POST /api/dispense/batch/{}/add-operation - Add operation to batch", id);
		DispenseBatchDTO batch = dispenseService.addOperationToBatch(id, request);
		return ResponseEntity.ok(batch);
	}

	@PostMapping("/batch/{id}/execute")
	public ResponseEntity<DispenseBatchDTO> executeBatch(@PathVariable Long id) {
		log.info("POST /api/dispense/batch/{}/execute - Execute batch", id);
		
		// Execute batch asynchronously
		CompletableFuture.runAsync(() -> {
			try {
				dispenseService.executeBatch(id);
			} catch (Exception e) {
				log.error("Error executing batch asynchronously", e);
			}
		});
		
		// Return the batch immediately (execution status will be sent via WebSocket)
		DispenseBatchDTO batch = dispenseService.getBatchById(id);
		return ResponseEntity.ok(batch);
	}
}
