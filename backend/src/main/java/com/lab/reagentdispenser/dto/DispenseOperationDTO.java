package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseOperationDTO {

	private Long id;

	@NotNull(message = "Plate ID is required")
	private Long plateId;

	private String plateBarcode;

	@NotNull(message = "Well ID is required")
	private Long wellId;

	private String wellPosition;

	private Double wellVolume; // Current volume in the well after dispense

	@NotNull(message = "Reagent ID is required")
	private Long reagentId;

	private String reagentName;

	@Min(value = 0, message = "Volume dispensed must be positive")
	private Double volumeDispensed;

	private String status;

	private LocalDateTime createdDate;

	private LocalDateTime completedDate;

	private String errorMessage;
}
