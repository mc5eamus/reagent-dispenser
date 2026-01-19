package com.lab.reagentdispenser.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event for dispense operation updates that should be broadcast via WebSocket
 * after the transaction commits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseEvent {
	
	private String messageType;
	private Object payload;
}
