package QuanLy.Chat.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.Entity.Notification;
import QuanLy.Chat.DTO.NotificationDTO;
import QuanLy.Chat.Service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> list(@PathVariable Long userId) {
        List<NotificationDTO> dtos = notificationService.listByUser(userId).stream()
            .map(n -> new NotificationDTO(n.getNotificationId(), n.getUser().getUserId(), n.getMessage(), n.getRead(), n.getCreatedAt()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
	}

	@GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> listUnread(@PathVariable Long userId) {
        List<NotificationDTO> dtos = notificationService.listUnreadByUser(userId).stream()
            .map(n -> new NotificationDTO(n.getNotificationId(), n.getUser().getUserId(), n.getMessage(), n.getRead(), n.getCreatedAt()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
	}

	@PostMapping
    public ResponseEntity<?> create(@RequestParam Long userId, @RequestParam String message) {
		try {
			Notification n = notificationService.create(userId, message);
            NotificationDTO dto = new NotificationDTO(n.getNotificationId(), n.getUser().getUserId(), n.getMessage(), n.getRead(), n.getCreatedAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		}
	}

	@PostMapping("/user/{userId}/mark-all-read")
	public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
		notificationService.markAllRead(userId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<Void> clearAll(@PathVariable Long userId) {
		notificationService.clearAll(userId);
		return ResponseEntity.noContent().build();
	}
}


