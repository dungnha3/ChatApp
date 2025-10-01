package QuanLy.Chat.DTO.webrtc;

public class AnswerDTO {
	private Long fromUserId;
	private Long toUserId;
	private String sdp;

	public AnswerDTO() {}

	public AnswerDTO(Long fromUserId, Long toUserId, String sdp) {
		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
		this.sdp = sdp;
	}

	public Long getFromUserId() { return fromUserId; }
	public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
	public Long getToUserId() { return toUserId; }
	public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
	public String getSdp() { return sdp; }
	public void setSdp(String sdp) { this.sdp = sdp; }
}


