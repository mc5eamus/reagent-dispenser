package com.lab.reagentdispenser.controller;

import com.lab.reagentdispenser.dto.PlateDTO;
import com.lab.reagentdispenser.dto.WellDTO;
import com.lab.reagentdispenser.service.PlateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/plates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class PlateController {

	private final PlateService plateService;

	@GetMapping
	public ResponseEntity<List<PlateDTO>> getAllPlates() {
		log.info("GET /api/plates - Get all plates");
		List<PlateDTO> plates = plateService.getAllPlates();
		return ResponseEntity.ok(plates);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PlateDTO> getPlateById(@PathVariable Long id) {
		log.info("GET /api/plates/{} - Get plate by id", id);
		PlateDTO plate = plateService.getPlateById(id);
		return ResponseEntity.ok(plate);
	}

	@GetMapping("/barcode/{barcode}")
	public ResponseEntity<PlateDTO> getPlateByBarcode(@PathVariable String barcode) {
		log.info("GET /api/plates/barcode/{} - Get plate by barcode", barcode);
		PlateDTO plate = plateService.getPlateByBarcode(barcode);
		return ResponseEntity.ok(plate);
	}

	@GetMapping("/{id}/wells")
	public ResponseEntity<List<WellDTO>> getWellsForPlate(@PathVariable Long id) {
		log.info("GET /api/plates/{}/wells - Get wells for plate", id);
		List<WellDTO> wells = plateService.getWellsForPlate(id);
		return ResponseEntity.ok(wells);
	}

	@PostMapping
	public ResponseEntity<PlateDTO> createPlate(@Valid @RequestBody PlateDTO plateDTO) {
		log.info("POST /api/plates - Create new plate");
		PlateDTO createdPlate = plateService.createPlate(plateDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPlate);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PlateDTO> updatePlate(
			@PathVariable Long id, 
			@Valid @RequestBody PlateDTO plateDTO) {
		log.info("PUT /api/plates/{} - Update plate", id);
		PlateDTO updatedPlate = plateService.updatePlate(id, plateDTO);
		return ResponseEntity.ok(updatedPlate);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePlate(@PathVariable Long id) {
		log.info("DELETE /api/plates/{} - Delete plate", id);
		plateService.deletePlate(id);
		return ResponseEntity.noContent().build();
	}
}
