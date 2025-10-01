package QuanLy.Chat.DTO;

import java.time.LocalDateTime;

public class CallDTO {
	private Long id;
	private Long callerId;
	private Long receiverId;
	private String callType;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String status;
	private String streamUrl;

	public CallDTO() {}

	public CallDTO(Long id, Long callerId, Long receiverId, String callType, LocalDateTime startTime, LocalDateTime endTime, String status, String streamUrl) {
		this.id = id;
		this.callerId = callerId;
		this.receiverId = receiverId;
		this.callType = callType;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.streamUrl = streamUrl;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getCallerId() { return callerId; }
	public void setCallerId(Long callerId) { this.callerId = callerId; }
	public Long getReceiverId() { return receiverId; }
	public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
	public String getCallType() { return callType; }
	public void setCallType(String callType) { this.callType = callType; }
	public LocalDateTime getStartTime() { return startTime; }
	public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
	public LocalDateTime getEndTime() { return endTime; }
	public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getStreamUrl() { return streamUrl; }
	public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
}


