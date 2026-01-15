package com.lab.reagentdispenser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

	private String type;

	private Object payload;

	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();
}
