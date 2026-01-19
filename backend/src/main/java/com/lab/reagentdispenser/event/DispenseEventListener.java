package com.lab.reagentdispenser.event;

import com.lab.reagentdispenser.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Event listener that sends WebSocket messages AFTER the transaction commits.
 * This ensures that clients fetching data after receiving a WebSocket message
 * will see the updated values.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DispenseEventListener {

	private final SimpMessagingTemplate messagingTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDispenseEvent(DispenseEvent event) {
		log.debug("Sending WebSocket message after transaction commit: {}", event.getMessageType());
		
		WebSocketMessage message = WebSocketMessage.builder()
				.type(event.getMessageType())
				.payload(event.getPayload())
				.timestamp(LocalDateTime.now())
				.build();

		messagingTemplate.convertAndSend("/topic/dispense-status", message);
		log.debug("Sent WebSocket message: {}", event.getMessageType());
	}
}
