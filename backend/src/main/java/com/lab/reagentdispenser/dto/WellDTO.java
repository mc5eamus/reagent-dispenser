package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WellDTO {

	private Long id;

	@NotBlank(message = "Position is required")
	@Pattern(regexp = "^[A-H]([1-9]|1[0-2])$", message = "Position must be in format A1-H12")
	private String position;

	private Long plateId;

	private Double volume;

	private Double maxVolume;
}
