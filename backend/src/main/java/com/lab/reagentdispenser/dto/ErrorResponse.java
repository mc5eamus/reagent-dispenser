package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private String message;

	private String details;

	private Integer status;

	private Long timestamp;

	public static ErrorResponse of(String message, String details, Integer status) {
		return ErrorResponse.builder()
				.message(message)
				.details(details)
				.status(status)
				.timestamp(System.currentTimeMillis())
				.build();
	}
}
