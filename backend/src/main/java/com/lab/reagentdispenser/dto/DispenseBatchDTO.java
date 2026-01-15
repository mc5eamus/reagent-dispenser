package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseBatchDTO {

	private Long id;
	private Long plateId;
	private String plateBarcode;
	private String status;
	private LocalDateTime createdDate;
	private LocalDateTime executionStartedDate;
	private LocalDateTime completedDate;
	private Integer operationCount;
	private List<DispenseOperationDTO> operations;
}
