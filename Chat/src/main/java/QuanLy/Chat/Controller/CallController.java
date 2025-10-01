package QuanLy.Chat.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.Entity.Call;
import QuanLy.Chat.Service.CallService;

@RestController
@RequestMapping("/api/calls")
public class CallController {

	private final CallService callService;

	public CallController(CallService callService) {
		this.callService = callService;
	}

	@PostMapping("/start")
	public ResponseEntity<?> start(@RequestParam Long callerId, @RequestParam Long receiverId, @RequestParam(required = false) String callType) {
		try {
			Call call = callService.startCall(callerId, receiverId, callType);
			return ResponseEntity.status(HttpStatus.CREATED).body(call);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}
	}

	@PostMapping("/{callId}/end")
	public ResponseEntity<Call> end(@PathVariable Long callId) {
		return ResponseEntity.ok(callService.endCall(callId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Call>> history(@PathVariable Long userId) {
		return ResponseEntity.ok(callService.listCallsOfUser(userId));
	}

	@DeleteMapping("/{callId}")
	public ResponseEntity<Void> delete(@PathVariable Long callId) {
		callService.deleteCall(callId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/range")
	public ResponseEntity<String> deleteRange(@RequestParam String from, @RequestParam String to) {
		int removed = callService.deleteCallsInRange(java.time.LocalDateTime.parse(from), java.time.LocalDateTime.parse(to));
		return ResponseEntity.ok("Removed: " + removed);
	}
}


