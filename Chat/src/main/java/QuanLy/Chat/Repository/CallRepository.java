package QuanLy.Chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import QuanLy.Chat.Entity.Call;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
	List<Call> findByCallerIdOrReceiverId(Long callerId, Long receiverId);
	void deleteById(Long id);
}
