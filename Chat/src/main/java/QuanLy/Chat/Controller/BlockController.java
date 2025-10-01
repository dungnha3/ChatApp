package QuanLy.Chat.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.Entity.Block;
import QuanLy.Chat.Service.BlockService;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

	private final BlockService blockService;

	public BlockController(BlockService blockService) {
		this.blockService = blockService;
	}

	@PostMapping
	public ResponseEntity<Block> block(@RequestParam Long blockerId, @RequestParam Long blockedId) {
		return ResponseEntity.ok(blockService.block(blockerId, blockedId));
	}

	@DeleteMapping
	public ResponseEntity<Void> unblock(@RequestParam Long blockerId, @RequestParam Long blockedId) {
		blockService.unblock(blockerId, blockedId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<Block>> list(@RequestParam Long blockerId) {
		return ResponseEntity.ok(blockService.list(blockerId));
	}
}


