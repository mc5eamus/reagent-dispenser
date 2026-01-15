package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReagentDTO {

	private Long id;

	@NotBlank(message = "Name is required")
	private String name;

	private String description;

	private String concentration;

	private Double stockVolume;

	private String unit;
}
