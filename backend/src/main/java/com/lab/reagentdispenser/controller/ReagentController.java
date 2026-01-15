package com.lab.reagentdispenser.controller;

import com.lab.reagentdispenser.dto.ReagentDTO;
import com.lab.reagentdispenser.service.ReagentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reagents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ReagentController {

	private final ReagentService reagentService;

	@GetMapping
	public ResponseEntity<List<ReagentDTO>> getAllReagents() {
		log.info("GET /api/reagents - Get all reagents");
		List<ReagentDTO> reagents = reagentService.getAllReagents();
		return ResponseEntity.ok(reagents);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReagentDTO> getReagentById(@PathVariable Long id) {
		log.info("GET /api/reagents/{} - Get reagent by id", id);
		ReagentDTO reagent = reagentService.getReagentById(id);
		return ResponseEntity.ok(reagent);
	}

	@PostMapping
	public ResponseEntity<ReagentDTO> createReagent(@Valid @RequestBody ReagentDTO reagentDTO) {
		log.info("POST /api/reagents - Create new reagent");
		ReagentDTO createdReagent = reagentService.createReagent(reagentDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdReagent);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ReagentDTO> updateReagent(
			@PathVariable Long id, 
			@Valid @RequestBody ReagentDTO reagentDTO) {
		log.info("PUT /api/reagents/{} - Update reagent", id);
		ReagentDTO updatedReagent = reagentService.updateReagent(id, reagentDTO);
		return ResponseEntity.ok(updatedReagent);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReagent(@PathVariable Long id) {
		log.info("DELETE /api/reagents/{} - Delete reagent", id);
		reagentService.deleteReagent(id);
		return ResponseEntity.noContent().build();
	}
}
