package com.lab.reagentdispenser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity
@Table(name = "wells", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"plate_id", "position"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Well {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Position is required")
	@Pattern(regexp = "^[A-H]([1-9]|1[0-2])$", message = "Position must be in format A1-H12")
	@Column(nullable = false)
	private String position;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plate_id", nullable = false)
	private Plate plate;

	@Column
	private Double volume;

	@Column(name = "max_volume", nullable = false)
	@Builder.Default
	private Double maxVolume = 300.0;
}
