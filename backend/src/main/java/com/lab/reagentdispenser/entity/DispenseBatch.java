package com.lab.reagentdispenser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispense_batches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseBatch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plate_id", nullable = false)
	@NotNull(message = "Plate is required")
	private Plate plate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private BatchStatus status = BatchStatus.PLANNED;

	@Column(name = "created_date", nullable = false)
	@Builder.Default
	private LocalDateTime createdDate = LocalDateTime.now();

	@Column(name = "execution_started_date")
	private LocalDateTime executionStartedDate;

	@Column(name = "completed_date")
	private LocalDateTime completedDate;

	@OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<DispenseOperation> operations = new ArrayList<>();

	public enum BatchStatus {
		PLANNED,      // Batch created, operations can be added
		EXECUTING,    // Batch execution in progress
		COMPLETED,    // All operations completed successfully
		FAILED        // One or more operations failed
	}

	// Helper method to add operation to batch
	public void addOperation(DispenseOperation operation) {
		operations.add(operation);
		operation.setBatch(this);
	}
}
