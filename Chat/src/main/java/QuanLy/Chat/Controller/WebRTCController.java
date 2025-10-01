package QuanLy.Chat.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import QuanLy.Chat.DTO.webrtc.AnswerDTO;
import QuanLy.Chat.DTO.webrtc.IceCandidateDTO;
import QuanLy.Chat.DTO.webrtc.OfferDTO;

@Controller
public class WebRTCController {

	private final SimpMessagingTemplate messagingTemplate;

	public WebRTCController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	// Gửi offer tới user nhận: client subscribe /topic/webrtc/{toUserId}/offer
	@MessageMapping("/webrtc/offer")
	public void offer(@Payload OfferDTO offer) {
		messagingTemplate.convertAndSend("/topic/webrtc/" + offer.getToUserId() + "/offer", offer);
	}

	// Gửi answer
	@MessageMapping("/webrtc/answer")
	public void answer(@Payload AnswerDTO answer) {
		messagingTemplate.convertAndSend("/topic/webrtc/" + answer.getToUserId() + "/answer", answer);
	}

	// Gửi ICE
	@MessageMapping("/webrtc/ice")
	public void ice(@Payload IceCandidateDTO ice) {
		messagingTemplate.convertAndSend("/topic/webrtc/" + ice.getToUserId() + "/ice", ice);
	}
}


