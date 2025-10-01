package QuanLy.Chat.Service;

import java.util.List;

import QuanLy.Chat.Entity.Block;

public interface BlockService {
	Block block(Long blockerId, Long blockedId);
	void unblock(Long blockerId, Long blockedId);
	boolean isBlocked(Long blockerId, Long blockedId);
	List<Block> list(Long blockerId);
}


