package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlateDTO {

	private Long id;

	@NotBlank(message = "Barcode is required")
	private String barcode;

	@Min(value = 1, message = "Rows must be at least 1")
	private Integer rows;

	@Min(value = 1, message = "Columns must be at least 1")
	private Integer columns;

	private String plateType;

	private LocalDateTime createdDate;

	private List<WellDTO> wells;
}
