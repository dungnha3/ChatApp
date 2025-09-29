package QuanLy.Chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import QuanLy.Chat.Entity.Friend;
import QuanLy.Chat.Entity.FriendID;

public interface FriendRepository extends JpaRepository<Friend, FriendID> {

    // Lấy tất cả bạn bè mà user này đã kết nối
    List<Friend> findByUserUserId(Long userId);

    // Lấy tất cả những user đã kết bạn với 1 người
    List<Friend> findByFriendUserId(Long friendId);

    // Kiểm tra xem 2 user đã kết bạn chưa
    boolean existsByUserUserIdAndFriendUserId(Long userId, Long friendId);
}
