package QuanLy.Chat.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long reporterId;

	@Column(nullable = false)
	private Long reportedUserId;

	@Column(length = 500)
	private String reason;

	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() { this.createdAt = LocalDateTime.now(); }

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getReporterId() { return reporterId; }
	public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
	public Long getReportedUserId() { return reportedUserId; }
	public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


