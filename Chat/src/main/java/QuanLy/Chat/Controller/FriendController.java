package QuanLy.Chat.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import QuanLy.Chat.DTO.FriendDTO;
import QuanLy.Chat.Service.FriendService;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/add")
    public ResponseEntity<?> addFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            FriendDTO dto = friendService.addFriend(userId, friendId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            FriendDTO dto = friendService.acceptFriend(userId, friendId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendDTO>> getFriends(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        try {
            friendService.deleteFriend(userId, friendId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejectFriend(@RequestParam Long userId, @RequestParam Long friendId) {
        friendService.rejectFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelRequest(@RequestParam Long userId, @RequestParam Long friendId) {
        friendService.cancelRequest(userId, friendId);
        return ResponseEntity.noContent().build();
    }
}
