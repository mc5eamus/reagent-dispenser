package com.lab.reagentdispenser.service;

import com.lab.reagentdispenser.dto.AddOperationToBatchRequestDTO;
import com.lab.reagentdispenser.dto.CreateBatchRequestDTO;
import com.lab.reagentdispenser.dto.DispenseBatchDTO;
import com.lab.reagentdispenser.dto.DispenseOperationDTO;
import com.lab.reagentdispenser.dto.DispenseRequestDTO;
import com.lab.reagentdispenser.dto.WebSocketMessage;
import com.lab.reagentdispenser.entity.DispenseBatch;
import com.lab.reagentdispenser.entity.DispenseOperation;
import com.lab.reagentdispenser.entity.Plate;
import com.lab.reagentdispenser.entity.Reagent;
import com.lab.reagentdispenser.entity.Well;
import com.lab.reagentdispenser.repository.DispenseBatchRepository;
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
	private final DispenseBatchRepository batchRepository;
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

	// Batch operations methods

	@Transactional
	public DispenseBatchDTO createBatch(CreateBatchRequestDTO request) {
		log.info("Creating dispense batch for plate: {}", request.getPlateBarcode());

		// Validate plate
		Plate plate = plateRepository.findByBarcode(request.getPlateBarcode())
				.orElseThrow(() -> new IllegalArgumentException(
						"Plate not found with barcode: " + request.getPlateBarcode()));

		// Create batch
		DispenseBatch batch = DispenseBatch.builder()
				.plate(plate)
				.status(DispenseBatch.BatchStatus.PLANNED)
				.createdDate(LocalDateTime.now())
				.build();

		DispenseBatch savedBatch = batchRepository.save(batch);
		log.info("Created batch with id: {}", savedBatch.getId());

		return convertBatchToDTO(savedBatch);
	}

	@Transactional
	public DispenseBatchDTO addOperationToBatch(Long batchId, AddOperationToBatchRequestDTO request) {
		log.info("Adding operation to batch: {}", batchId);

		// Validate batch
		DispenseBatch batch = batchRepository.findById(batchId)
				.orElseThrow(() -> new IllegalArgumentException("Batch not found with id: " + batchId));

		if (batch.getStatus() != DispenseBatch.BatchStatus.PLANNED) {
			throw new IllegalStateException("Cannot add operations to batch with status: " + batch.getStatus());
		}

		Plate plate = batch.getPlate();

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

		batch.addOperation(operation);
		DispenseBatch savedBatch = batchRepository.save(batch);

		log.info("Added operation to batch: {}, total operations: {}", batchId, savedBatch.getOperations().size());

		return convertBatchToDTO(savedBatch);
	}

	public DispenseBatchDTO getBatchById(Long batchId) {
		log.info("Retrieving batch by id: {}", batchId);
		DispenseBatch batch = batchRepository.findById(batchId)
				.orElseThrow(() -> new IllegalArgumentException("Batch not found with id: " + batchId));
		return convertBatchToDTO(batch);
	}

	public List<DispenseBatchDTO> getAllBatches() {
		log.info("Retrieving all dispense batches");
		return batchRepository.findAll().stream()
				.map(this::convertBatchToDTO)
				.collect(Collectors.toList());
	}

	@Transactional
	public DispenseBatchDTO executeBatch(Long batchId) {
		log.info("Executing batch with id: {}", batchId);

		DispenseBatch batch = batchRepository.findById(batchId)
				.orElseThrow(() -> new IllegalArgumentException("Batch not found with id: " + batchId));

		if (batch.getStatus() != DispenseBatch.BatchStatus.PLANNED) {
			throw new IllegalStateException("Batch is not in PLANNED status: " + batch.getStatus());
		}

		if (batch.getOperations().isEmpty()) {
			throw new IllegalStateException("Batch has no operations to execute");
		}

		// Update batch status to EXECUTING
		batch.setStatus(DispenseBatch.BatchStatus.EXECUTING);
		batch.setExecutionStartedDate(LocalDateTime.now());
		batchRepository.save(batch);

		// Send WebSocket notification about batch execution start
		sendWebSocketUpdate("BATCH_EXECUTION_STARTED", convertBatchToDTO(batch));

		// Execute operations sequentially with 0.5s delay
		boolean allSuccess = true;
		for (DispenseOperation operation : batch.getOperations()) {
			try {
				executeOperationInBatch(operation);
				
				// Add 0.5 second delay between operations
				Thread.sleep(500);
				
			} catch (Exception e) {
				log.error("Failed to execute operation with id: {}", operation.getId(), e);
				allSuccess = false;
				operation.setStatus(DispenseOperation.OperationStatus.FAILED);
				operation.setErrorMessage(e.getMessage());
				operation.setCompletedDate(LocalDateTime.now());
				operationRepository.save(operation);
				sendWebSocketUpdate("OPERATION_STATUS_CHANGE", convertToDTO(operation));
			}
		}

		// Update batch status
		batch.setStatus(allSuccess ? DispenseBatch.BatchStatus.COMPLETED : DispenseBatch.BatchStatus.FAILED);
		batch.setCompletedDate(LocalDateTime.now());
		DispenseBatch completedBatch = batchRepository.save(batch);

		log.info("Completed batch execution with id: {}, status: {}", batchId, batch.getStatus());
		sendWebSocketUpdate("BATCH_EXECUTION_COMPLETED", convertBatchToDTO(completedBatch));

		return convertBatchToDTO(completedBatch);
	}

	private void executeOperationInBatch(DispenseOperation operation) throws Exception {
		log.info("Executing operation with id: {} in batch", operation.getId());

		// Update status to IN_PROGRESS
		operation.setStatus(DispenseOperation.OperationStatus.IN_PROGRESS);
		operationRepository.save(operation);
		sendWebSocketUpdate("OPERATION_STATUS_CHANGE", convertToDTO(operation));

		// Simulate dispense operation (in real system, this would communicate with hardware)
		// No additional sleep here since we're adding 0.5s between operations

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

		log.info("Completed operation with id: {}", operation.getId());
		sendWebSocketUpdate("OPERATION_STATUS_CHANGE", convertToDTO(operation));
	}

	private void sendWebSocketUpdate(String messageType, DispenseBatchDTO batchDTO) {
		WebSocketMessage message = WebSocketMessage.builder()
				.type(messageType)
				.payload(batchDTO)
				.timestamp(LocalDateTime.now())
				.build();

		messagingTemplate.convertAndSend("/topic/dispense-status", message);
		log.debug("Sent WebSocket message: {}", messageType);
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

	private DispenseBatchDTO convertBatchToDTO(DispenseBatch batch) {
		return DispenseBatchDTO.builder()
				.id(batch.getId())
				.plateId(batch.getPlate().getId())
				.plateBarcode(batch.getPlate().getBarcode())
				.status(batch.getStatus().name())
				.createdDate(batch.getCreatedDate())
				.executionStartedDate(batch.getExecutionStartedDate())
				.completedDate(batch.getCompletedDate())
				.operationCount(batch.getOperations().size())
				.operations(batch.getOperations().stream()
						.map(this::convertToDTO)
						.collect(Collectors.toList()))
				.build();
	}
}
