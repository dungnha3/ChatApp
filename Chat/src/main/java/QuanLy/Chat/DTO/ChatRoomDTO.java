package QuanLy.Chat.DTO;

import java.time.LocalDateTime;

public class ChatRoomDTO {
	private Long chatRoomId;
	private String roomName;
	private Boolean isGroup;
	private LocalDateTime createdAt;
	private Integer membersCount;

	public ChatRoomDTO() {}

	public ChatRoomDTO(Long chatRoomId, String roomName, Boolean isGroup, LocalDateTime createdAt, Integer membersCount) {
		this.chatRoomId = chatRoomId;
		this.roomName = roomName;
		this.isGroup = isGroup;
		this.createdAt = createdAt;
		this.membersCount = membersCount;
	}

	public Long getChatRoomId() { return chatRoomId; }
	public void setChatRoomId(Long chatRoomId) { this.chatRoomId = chatRoomId; }
	public String getRoomName() { return roomName; }
	public void setRoomName(String roomName) { this.roomName = roomName; }
	public Boolean getIsGroup() { return isGroup; }
	public void setIsGroup(Boolean isGroup) { this.isGroup = isGroup; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public Integer getMembersCount() { return membersCount; }
	public void setMembersCount(Integer membersCount) { this.membersCount = membersCount; }
}


