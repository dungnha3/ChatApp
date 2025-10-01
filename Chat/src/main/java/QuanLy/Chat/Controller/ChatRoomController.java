package QuanLy.Chat.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.Entity.ChatRoom;
import QuanLy.Chat.Entity.ChatRoomMember;
import QuanLy.Chat.Service.ChatRoomService;
import QuanLy.Chat.DTO.ChatRoomDTO;

@RestController
@RequestMapping("/api/rooms")
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	public ChatRoomController(ChatRoomService chatRoomService) {
		this.chatRoomService = chatRoomService;
	}

	@PostMapping
    public ResponseEntity<?> createRoom(@RequestParam String roomName, @RequestParam(defaultValue = "false") boolean isGroup) {
		try {
			ChatRoom room = chatRoomService.createRoom(roomName, isGroup);
            ChatRoomDTO dto = new ChatRoomDTO(room.getChatRoomId(), room.getRoomName(), room.getIsGroup(), room.getCreatedAt(), room.getMembers() == null ? 0 : room.getMembers().size());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}
	}

	@PutMapping("/{roomId}")
    public ResponseEntity<ChatRoomDTO> updateRoom(@PathVariable Long roomId, @RequestParam String roomName) {
        ChatRoom room = chatRoomService.updateRoom(roomId, roomName);
        ChatRoomDTO dto = new ChatRoomDTO(room.getChatRoomId(), room.getRoomName(), room.getIsGroup(), room.getCreatedAt(), room.getMembers() == null ? 0 : room.getMembers().size());
        return ResponseEntity.ok(dto);
	}

	@DeleteMapping("/{roomId}")
	public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
		chatRoomService.deleteRoom(roomId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDTO> getRoom(@PathVariable Long roomId) {
        ChatRoom room = chatRoomService.getRoom(roomId);
        ChatRoomDTO dto = new ChatRoomDTO(room.getChatRoomId(), room.getRoomName(), room.getIsGroup(), room.getCreatedAt(), room.getMembers() == null ? 0 : room.getMembers().size());
        return ResponseEntity.ok(dto);
	}

	@GetMapping
    public ResponseEntity<List<ChatRoomDTO>> getAll() {
        List<ChatRoomDTO> dtos = chatRoomService.getAllRooms().stream()
            .map(r -> new ChatRoomDTO(r.getChatRoomId(), r.getRoomName(), r.getIsGroup(), r.getCreatedAt(), r.getMembers() == null ? 0 : r.getMembers().size()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
	}

	@PostMapping("/{roomId}/members")
	public ResponseEntity<ChatRoomMember> addMember(@PathVariable Long roomId, @RequestParam Long userId, @RequestParam(required = false) String role) {
		return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomService.addMember(roomId, userId, role));
	}

	@DeleteMapping("/{roomId}/members/{userId}")
	public ResponseEntity<Void> removeMember(@PathVariable Long roomId, @PathVariable Long userId) {
		chatRoomService.removeMember(roomId, userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{roomId}/members")
	public ResponseEntity<List<ChatRoomMember>> listMembers(@PathVariable Long roomId) {
		return ResponseEntity.ok(chatRoomService.listMembers(roomId));
	}
}


