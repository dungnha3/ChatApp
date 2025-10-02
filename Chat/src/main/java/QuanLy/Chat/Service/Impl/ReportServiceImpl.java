package QuanLy.Chat.Service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import QuanLy.Chat.Entity.Report;
import QuanLy.Chat.Repository.ReportRepository;
import QuanLy.Chat.Service.ReportService;

@Service
public class ReportServiceImpl implements ReportService {

	private final ReportRepository reportRepository;

	public ReportServiceImpl(ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}

	@Override
	public Report createReport(Long reporterId, Long reportedUserId, String reason) {
		Report report = new Report();
		report.setReporterId(reporterId);
		report.setReportedUserId(reportedUserId);
		report.setReason(reason);
		return reportRepository.save(report);
	}

	@Override
	public List<Report> getAllReports() {
		return reportRepository.findAll();
	}

	@Override
	public void deleteReport(Long id) {
		reportRepository.deleteById(id);
	}
}





