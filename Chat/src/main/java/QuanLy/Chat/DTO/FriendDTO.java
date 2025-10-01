package QuanLy.Chat.DTO;

public class FriendDTO {
    private Long userId;
    private Long friendId;
    private String status;

    public FriendDTO() {}

    public FriendDTO(Long userId, Long friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    // Getter & Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getFriendId() { return friendId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
