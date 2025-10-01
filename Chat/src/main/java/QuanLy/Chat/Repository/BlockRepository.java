package QuanLy.Chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import QuanLy.Chat.Entity.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
	boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
	List<Block> findByBlockerId(Long blockerId);
}


