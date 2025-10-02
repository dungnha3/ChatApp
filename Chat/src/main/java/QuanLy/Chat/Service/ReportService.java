package QuanLy.Chat.Service;

import java.util.List;

import QuanLy.Chat.Entity.Report;

public interface ReportService {
	Report createReport(Long reporterId, Long reportedUserId, String reason);
	List<Report> getAllReports();
	void deleteReport(Long id);
}





