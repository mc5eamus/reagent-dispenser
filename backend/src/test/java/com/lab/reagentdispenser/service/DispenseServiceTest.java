package com.lab.reagentdispenser.service;

import com.lab.reagentdispenser.dto.DispenseOperationDTO;
import com.lab.reagentdispenser.entity.DispenseOperation;
import com.lab.reagentdispenser.entity.Plate;
import com.lab.reagentdispenser.entity.Reagent;
import com.lab.reagentdispenser.entity.Well;
import com.lab.reagentdispenser.repository.DispenseBatchRepository;
import com.lab.reagentdispenser.repository.DispenseOperationRepository;
import com.lab.reagentdispenser.repository.PlateRepository;
import com.lab.reagentdispenser.repository.ReagentRepository;
import com.lab.reagentdispenser.repository.WellRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispenseServiceTest {

	@Mock
	private DispenseOperationRepository operationRepository;

	@Mock
	private DispenseBatchRepository batchRepository;

	@Mock
	private PlateRepository plateRepository;

	@Mock
	private WellRepository wellRepository;

	@Mock
	private ReagentRepository reagentRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private DispenseService dispenseService;

	@Test
	void shouldIncludeWellVolumeInDTOAfterExecutingOperation() throws Exception {
		// Arrange
		Plate plate = Plate.builder()
				.id(1L)
				.barcode("TEST-PLATE-001")
				.rows(8)
				.columns(12)
				.build();

		Well well = Well.builder()
				.id(1L)
				.position("A1")
				.plate(plate)
				.volume(50.0) // Initial volume
				.maxVolume(200.0)
				.build();

		Reagent reagent = Reagent.builder()
				.id(1L)
				.name("Test Reagent")
				.stockVolume(1000.0)
				.build();

		DispenseOperation operation = DispenseOperation.builder()
				.id(1L)
				.plate(plate)
				.well(well)
				.reagent(reagent)
				.volumeDispensed(25.0)
				.status(DispenseOperation.OperationStatus.PENDING)
				.createdDate(LocalDateTime.now())
				.build();

		// Mock repository calls
		when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));
		when(operationRepository.save(any(DispenseOperation.class))).thenAnswer(invocation -> {
			DispenseOperation op = invocation.getArgument(0);
			// Update well volume when operation is saved as COMPLETED
			if (op.getStatus() == DispenseOperation.OperationStatus.COMPLETED) {
				well.setVolume(75.0); // 50.0 + 25.0
			}
			return op;
		});
		when(wellRepository.save(any(Well.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(reagentRepository.save(any(Reagent.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		DispenseOperationDTO result = dispenseService.executeOperation(1L);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getWellVolume()).isNotNull();
		assertThat(result.getWellVolume()).isEqualTo(75.0); // Should reflect updated volume
		assertThat(result.getStatus()).isEqualTo("COMPLETED");
		assertThat(result.getWellPosition()).isEqualTo("A1");
		assertThat(result.getVolumeDispensed()).isEqualTo(25.0);

		// Verify WebSocket message was sent
		verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/dispense-status"), any(Object.class));
	}

	@Test
	void shouldIncludeWellVolumeInDTOWhenGettingOperation() {
		// Arrange
		Plate plate = Plate.builder()
				.id(1L)
				.barcode("TEST-PLATE-001")
				.rows(8)
				.columns(12)
				.build();

		Well well = Well.builder()
				.id(1L)
				.position("B3")
				.plate(plate)
				.volume(100.0) // Current volume
				.maxVolume(200.0)
				.build();

		Reagent reagent = Reagent.builder()
				.id(1L)
				.name("Test Reagent")
				.stockVolume(1000.0)
				.build();

		DispenseOperation operation = DispenseOperation.builder()
				.id(1L)
				.plate(plate)
				.well(well)
				.reagent(reagent)
				.volumeDispensed(50.0)
				.status(DispenseOperation.OperationStatus.COMPLETED)
				.createdDate(LocalDateTime.now())
				.completedDate(LocalDateTime.now())
				.build();

		when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

		// Act
		DispenseOperationDTO result = dispenseService.getOperationById(1L);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getWellVolume()).isEqualTo(100.0);
		assertThat(result.getWellPosition()).isEqualTo("B3");
		assertThat(result.getVolumeDispensed()).isEqualTo(50.0);
	}
}
