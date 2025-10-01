package QuanLy.Chat.DTO.webrtc;

public class IceCandidateDTO {
	private Long fromUserId;
	private Long toUserId;
	private String candidate;

	public IceCandidateDTO() {}

	public IceCandidateDTO(Long fromUserId, Long toUserId, String candidate) {
		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
		this.candidate = candidate;
	}

	public Long getFromUserId() { return fromUserId; }
	public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
	public Long getToUserId() { return toUserId; }
	public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
	public String getCandidate() { return candidate; }
	public void setCandidate(String candidate) { this.candidate = candidate; }
}


