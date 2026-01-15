package com.lab.reagentdispenser.service;

import com.lab.reagentdispenser.dto.DispenseOperationDTO;
import com.lab.reagentdispenser.dto.DispenseRequestDTO;
import com.lab.reagentdispenser.dto.WebSocketMessage;
import com.lab.reagentdispenser.entity.DispenseOperation;
import com.lab.reagentdispenser.entity.Plate;
import com.lab.reagentdispenser.entity.Reagent;
import com.lab.reagentdispenser.entity.Well;
import com.lab.reagentdispenser.repository.DispenseOperationRepository;
import com.lab.reagentdispenser.repository.PlateRepository;
import com.lab.reagentdispenser.repository.ReagentRepository;
import com.lab.reagentdispenser.repository.WellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispenseService {

	private final DispenseOperationRepository operationRepository;
	private final PlateRepository plateRepository;
	private final WellRepository wellRepository;
	private final ReagentRepository reagentRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public List<DispenseOperationDTO> getAllOperations() {
		log.info("Retrieving all dispense operations");
		return operationRepository.findAll().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public DispenseOperationDTO getOperationById(Long id) {
		log.info("Retrieving operation by id: {}", id);
		DispenseOperation operation = operationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Operation not found with id: " + id));
		return convertToDTO(operation);
	}

	public List<DispenseOperationDTO> getOperationsByStatus(String status) {
		log.info("Retrieving operations by status: {}", status);
		DispenseOperation.OperationStatus operationStatus = DispenseOperation.OperationStatus.valueOf(status);
		return operationRepository.findByStatus(operationStatus).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	@Transactional
	public DispenseOperationDTO createOperation(DispenseRequestDTO request) {
		log.info("Creating dispense operation for plate: {}, well: {}", 
				request.getPlateBarcode(), request.getWellPosition());

		// Validate plate
		Plate plate = plateRepository.findByBarcode(request.getPlateBarcode())
				.orElseThrow(() -> new IllegalArgumentException(
						"Plate not found with barcode: " + request.getPlateBarcode()));

		// Validate well
		Well well = wellRepository.findByPlateAndPosition(plate, request.getWellPosition())
				.orElseThrow(() -> new IllegalArgumentException(
						"Well not found at position: " + request.getWellPosition()));

		// Validate reagent
		Reagent reagent = reagentRepository.findById(request.getReagentId())
				.orElseThrow(() -> new IllegalArgumentException(
						"Reagent not found with id: " + request.getReagentId()));

		// Validate volume
		if (well.getVolume() != null && well.getVolume() + request.getVolume() > well.getMaxVolume()) {
			throw new IllegalArgumentException(
					"Volume exceeds well capacity. Current: " + well.getVolume() + 
					", Requested: " + request.getVolume() + 
					", Max: " + well.getMaxVolume());
		}

		// Validate reagent stock
		if (reagent.getStockVolume() < request.getVolume()) {
			throw new IllegalArgumentException(
					"Insufficient reagent stock. Available: " + reagent.getStockVolume() + 
					", Requested: " + request.getVolume());
		}

		// Create operation
		DispenseOperation operation = DispenseOperation.builder()
				.plate(plate)
				.well(well)
				.reagent(reagent)
				.volumeDispensed(request.getVolume())
				.status(DispenseOperation.OperationStatus.PENDING)
				.createdDate(LocalDateTime.now())
				.build();

		DispenseOperation savedOperation = operationRepository.save(operation);
		log.info("Created operation with id: {}", savedOperation.getId());

		// Send WebSocket notification
		sendWebSocketUpdate("OPERATION_CREATED", convertToDTO(savedOperation));

		return convertToDTO(savedOperation);
	}

	@Transactional
	public DispenseOperationDTO executeOperation(Long operationId) {
		log.info("Executing operation with id: {}", operationId);

		DispenseOperation operation = operationRepository.findById(operationId)
				.orElseThrow(() -> new IllegalArgumentException("Operation not found with id: " + operationId));

		if (operation.getStatus() != DispenseOperation.OperationStatus.PENDING) {
			throw new IllegalStateException("Operation is not in PENDING status: " + operation.getStatus());
		}

		// Update status to IN_PROGRESS
		operation.setStatus(DispenseOperation.OperationStatus.IN_PROGRESS);
		operationRepository.save(operation);
		sendWebSocketUpdate("OPERATION_STATUS_CHANGE", convertToDTO(operation));

		try {
			// Simulate dispense operation (in real system, this would communicate with hardware)
			Thread.sleep(2000); // Simulate 2 second dispense time

			// Update well volume
			Well well = operation.getWell();
			Double currentVolume = well.getVolume() != null ? well.getVolume() : 0.0;
			well.setVolume(currentVolume + operation.getVolumeDispensed());
			wellRepository.save(well);

			// Update reagent stock
			Reagent reagent = operation.getReagent();
			reagent.setStockVolume(reagent.getStockVolume() - operation.getVolumeDispensed());
			reagentRepository.save(reagent);

			// Update operation status to COMPLETED
			operation.setStatus(DispenseOperation.OperationStatus.COMPLETED);
			operation.setCompletedDate(LocalDateTime.now());
			operationRepository.save(operation);

			log.info("Completed operation with id: {}", operationId);
			sendWebSocketUpdate("OPERATION_STATUS_CHANGE", convertToDTO(operation));

		} catch (Exception e) {
			log.error("Failed to execute operation with id: {}", operationId, e);
			operation.setStatus(DispenseOperation.OperationStatus.FAILED);
			operation.setErrorMessage(e.getMessage());
			operation.setCompletedDate(LocalDateTime.now());
			operationRepository.save(operation);
			sendWebSocketUpdate("OPERATION_STATUS_CHANGE", convertToDTO(operation));
		}

		return convertToDTO(operation);
	}

	private void sendWebSocketUpdate(String messageType, DispenseOperationDTO operationDTO) {
		WebSocketMessage message = WebSocketMessage.builder()
				.type(messageType)
				.payload(operationDTO)
				.timestamp(LocalDateTime.now())
				.build();

		messagingTemplate.convertAndSend("/topic/dispense-status", message);
		log.debug("Sent WebSocket message: {}", messageType);
	}

	private DispenseOperationDTO convertToDTO(DispenseOperation operation) {
		return DispenseOperationDTO.builder()
				.id(operation.getId())
				.plateId(operation.getPlate().getId())
				.plateBarcode(operation.getPlate().getBarcode())
				.wellId(operation.getWell().getId())
				.wellPosition(operation.getWell().getPosition())
				.reagentId(operation.getReagent().getId())
				.reagentName(operation.getReagent().getName())
				.volumeDispensed(operation.getVolumeDispensed())
				.status(operation.getStatus().name())
				.createdDate(operation.getCreatedDate())
				.completedDate(operation.getCompletedDate())
				.errorMessage(operation.getErrorMessage())
				.build();
	}
}
