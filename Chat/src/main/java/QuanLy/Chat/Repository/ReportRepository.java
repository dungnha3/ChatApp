package QuanLy.Chat.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import QuanLy.Chat.Entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}


