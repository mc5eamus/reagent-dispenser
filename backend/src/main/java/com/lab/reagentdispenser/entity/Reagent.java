package com.lab.reagentdispenser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "reagents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reagent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is required")
	@Column(nullable = false)
	private String name;

	@Column(length = 1000)
	private String description;

	@Column(length = 100)
	private String concentration;

	@Column(name = "stock_volume", nullable = false)
	@Builder.Default
	private Double stockVolume = 0.0;

	@Column(nullable = false)
	@Builder.Default
	private String unit = "μL";
}
