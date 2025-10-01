package QuanLy.Chat.Service.Impl;

import QuanLy.Chat.Service.FriendService;
import QuanLy.Chat.DTO.FriendDTO;
import QuanLy.Chat.Entity.Friend;
import QuanLy.Chat.Entity.User;
import QuanLy.Chat.Repository.FriendRepository;
import QuanLy.Chat.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendServiceImpl(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FriendDTO addFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new IllegalArgumentException("Thiếu tham số userId hoặc friendId");
        }
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Không thể tự kết bạn với chính mình");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bạn bè với ID: " + friendId));

        if (friendRepository.existsByUser_UserIdAndFriend_UserId(userId, friendId)
                || friendRepository.existsByUser_UserIdAndFriend_UserId(friendId, userId)) {
            throw new RuntimeException("Đã tồn tại lời mời hoặc quan hệ bạn bè");
        }

        Friend relation = new Friend(user, friend, "pending");
        friendRepository.save(relation);

        return new FriendDTO(user.getUserId(), friend.getUserId(), relation.getStatus());
    }

    @Override
    public FriendDTO acceptFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new IllegalArgumentException("Thiếu tham số userId hoặc friendId");
        }
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bạn bè với ID: " + friendId));

        // Lời mời có thể được tạo bởi user -> friend hoặc friend -> user
        Friend relation = friendRepository.findByUser_UserIdAndFriend_UserId(userId, friendId)
                .orElseGet(() -> friendRepository.findByUser_UserIdAndFriend_UserId(friendId, userId)
                        .orElseThrow(() -> new RuntimeException("Không tồn tại lời mời kết bạn giữa " + userId + " và " + friendId)));
        relation.setStatus("accepted");
        friendRepository.save(relation);

        // Đảm bảo quan hệ 2 chiều khi đã accepted
        boolean reverseExists = friendRepository.existsByUser_UserIdAndFriend_UserId(friendId, userId);
        if (!reverseExists) {
            Friend reverse = new Friend(friend, user, "accepted");
            friendRepository.save(reverse);
        }

        return new FriendDTO(user.getUserId(), friend.getUserId(), relation.getStatus());
    }

    @Override
    public void rejectFriend(Long userId, Long friendId) {
        friendRepository.findByUser_UserIdAndFriend_UserId(friendId, userId)
                .ifPresent(friendRepository::delete);
    }

    @Override
    public void cancelRequest(Long userId, Long friendId) {
        friendRepository.findByUser_UserIdAndFriend_UserId(userId, friendId)
                .ifPresent(friendRepository::delete);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        Friend relation = friendRepository.findByUser_UserIdAndFriend_UserId(userId, friendId)
                .orElseGet(() -> friendRepository.findByUser_UserIdAndFriend_UserId(friendId, userId)
                        .orElseThrow(() -> new RuntimeException("Không tồn tại quan hệ bạn bè giữa " + userId + " và " + friendId)));
        friendRepository.delete(relation);

        // Xóa cả chiều ngược lại nếu tồn tại
        friendRepository.findByUser_UserIdAndFriend_UserId(friendId, userId)
                .ifPresent(friendRepository::delete);
    }

    @Override
    public List<FriendDTO> getFriends(Long userId) {
        return friendRepository.findByUser_UserId(userId)
                .stream()
                .filter(f -> "accepted".equalsIgnoreCase(f.getStatus()))
                .map(f -> new FriendDTO(f.getUser().getUserId(), f.getFriend().getUserId(), f.getStatus()))
                .collect(Collectors.toList());
    }
}
