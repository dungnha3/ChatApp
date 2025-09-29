package QuanLy.Chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import QuanLy.Chat.Entity.ChatRoom;
import QuanLy.Chat.Entity.Message;
import QuanLy.Chat.Entity.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy toàn bộ tin nhắn trong 1 phòng chat
    List<Message> findByChatRoom(ChatRoom chatRoom);

    // Lấy tất cả tin nhắn của 1 người dùng
    List<Message> findBySender(User sender);

    // Lấy tin nhắn mới nhất trong 1 phòng
    Message findTopByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);
}
