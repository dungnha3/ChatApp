package QuanLy.Chat.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webrtc")
public class IceServerController {

	@Value("${app.webrtc.stunUrls:stun:stun.l.google.com:19302}")
	private String stunUrls;

	@Value("${app.webrtc.turnUrl:}")
	private String turnUrl;

	@Value("${app.webrtc.turnUsername:}")
	private String turnUsername;

	@Value("${app.webrtc.turnCredential:}")
	private String turnCredential;

	@GetMapping("/ice-servers")
	public ResponseEntity<Map<String, Object>> iceServers() {
		List<Map<String, Object>> servers = new ArrayList<>();
		for (String url : stunUrls.split(",")) {
			Map<String, Object> stun = new HashMap<>();
			stun.put("urls", url.trim());
			servers.add(stun);
		}
		if (turnUrl != null && !turnUrl.isBlank()) {
			Map<String, Object> turn = new HashMap<>();
			turn.put("urls", turnUrl);
			if (turnUsername != null && !turnUsername.isBlank()) turn.put("username", turnUsername);
			if (turnCredential != null && !turnCredential.isBlank()) turn.put("credential", turnCredential);
			servers.add(turn);
		}
		Map<String, Object> resp = new HashMap<>();
		resp.put("iceServers", servers);
		return ResponseEntity.ok(resp);
	}
}


