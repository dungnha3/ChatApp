package QuanLy.Chat.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.Entity.Report;
import QuanLy.Chat.Service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@PostMapping
	public ResponseEntity<Report> createReport(@RequestParam Long reporterId, @RequestParam Long reportedUserId, @RequestParam(required = false) String reason) {
		Report report = reportService.createReport(reporterId, reportedUserId, reason);
		return ResponseEntity.status(HttpStatus.CREATED).body(report);
	}

	@GetMapping
	public ResponseEntity<List<Report>> getAllReports() {
		return ResponseEntity.ok(reportService.getAllReports());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
		reportService.deleteReport(id);
		return ResponseEntity.noContent().build();
	}
}





