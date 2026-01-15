package com.lab.reagentdispenser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispense_operations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseOperation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plate_id", nullable = false)
	@NotNull(message = "Plate is required")
	private Plate plate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "well_id", nullable = false)
	@NotNull(message = "Well is required")
	private Well well;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reagent_id", nullable = false)
	@NotNull(message = "Reagent is required")
	private Reagent reagent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "batch_id")
	private DispenseBatch batch;

	@Min(value = 0, message = "Volume dispensed must be positive")
	@Column(name = "volume_dispensed", nullable = false)
	private Double volumeDispensed;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private OperationStatus status = OperationStatus.PENDING;

	@Column(name = "created_date", nullable = false)
	@Builder.Default
	private LocalDateTime createdDate = LocalDateTime.now();

	@Column(name = "completed_date")
	private LocalDateTime completedDate;

	@Column(name = "error_message", length = 1000)
	private String errorMessage;

	public enum OperationStatus {
		PENDING,
		IN_PROGRESS,
		COMPLETED,
		FAILED
	}
}
