package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseRequestDTO {

	@NotBlank(message = "Plate barcode is required")
	private String plateBarcode;

	@NotBlank(message = "Well position is required")
	private String wellPosition;

	@NotNull(message = "Reagent ID is required")
	private Long reagentId;

	@NotNull(message = "Volume is required")
	@Min(value = 0, message = "Volume must be positive")
	private Double volume;
}
