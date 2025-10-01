package QuanLy.Chat.Controller;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.DTO.TypingDTO;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

	private final SimpMessagingTemplate messagingTemplate;
	private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

	public PresenceController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	// WS: client gửi /app/typing với body TypingDTO
	@MessageMapping("/typing")
	public void typing(@Payload TypingDTO typing) {
		messagingTemplate.convertAndSend("/topic/rooms/" + typing.getRoomId() + "/typing", typing);
	}

	// REST: get online status simple demo (server-only memory)
	@GetMapping("/online/{userId}")
	public ResponseEntity<Boolean> isOnline(@PathVariable Long userId) {
		return ResponseEntity.ok(onlineUsers.contains(userId));
	}
}


