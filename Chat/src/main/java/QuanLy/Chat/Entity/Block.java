package QuanLy.Chat.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocks")
public class Block {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long blockerId;

	@Column(nullable = false)
	private Long blockedId;

	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() { this.createdAt = LocalDateTime.now(); }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBlockerId() { return blockerId; }
	public void setBlockerId(Long blockerId) { this.blockerId = blockerId; }
	public Long getBlockedId() { return blockedId; }
	public void setBlockedId(Long blockedId) { this.blockedId = blockedId; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


