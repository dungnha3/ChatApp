package QuanLy.Chat.Service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import QuanLy.Chat.Entity.Block;
import QuanLy.Chat.Repository.BlockRepository;
import QuanLy.Chat.Service.BlockService;

@Service
public class BlockServiceImpl implements BlockService {

	private final BlockRepository blockRepository;

	public BlockServiceImpl(BlockRepository blockRepository) {
		this.blockRepository = blockRepository;
	}

	@Override
	public Block block(Long blockerId, Long blockedId) {
		if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
			return blockRepository.findByBlockerId(blockerId).stream().filter(b -> b.getBlockedId().equals(blockedId)).findFirst().get();
		}
		Block b = new Block();
		b.setBlockerId(blockerId);
		b.setBlockedId(blockedId);
		return blockRepository.save(b);
	}

	@Override
	public void unblock(Long blockerId, Long blockedId) {
		blockRepository.findByBlockerId(blockerId).stream()
			.filter(b -> b.getBlockedId().equals(blockedId))
			.findFirst()
			.ifPresent(existing -> blockRepository.deleteById(existing.getId()));
	}

	@Override
	public boolean isBlocked(Long blockerId, Long blockedId) {
		return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
	}

	@Override
	public List<Block> list(Long blockerId) {
		return blockRepository.findByBlockerId(blockerId);
	}
}


