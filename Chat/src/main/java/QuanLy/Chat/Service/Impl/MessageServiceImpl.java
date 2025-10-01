package QuanLy.Chat.Service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import QuanLy.Chat.Entity.ChatRoom;
import QuanLy.Chat.Entity.Message;
import QuanLy.Chat.Entity.User;
import QuanLy.Chat.Repository.ChatRoomRepository;
import QuanLy.Chat.Repository.MessageRepository;
import QuanLy.Chat.Repository.UserRepository;
import QuanLy.Chat.Service.MessageService;
import java.time.LocalDateTime;

@Service
public class MessageServiceImpl implements MessageService {

	private final MessageRepository messageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;

	public MessageServiceImpl(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
		this.messageRepository = messageRepository;
		this.chatRoomRepository = chatRoomRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Message sendMessage(Long roomId, Long senderId, String content) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Nội dung tin nhắn trống");
		}
		ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
		User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
		Message msg = new Message(room, sender, content);
		return messageRepository.save(msg);
	}

	@Override
	public List<Message> listMessagesByRoom(Long roomId) {
		ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
		return messageRepository.findByChatRoom(room);
	}

	@Override
	public List<Message> listMessagesByRoom(Long roomId, int page, int size) {
		ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
		return messageRepository.findByChatRoom(room, PageRequest.of(page, size));
	}

	@Override
	public Message editMessage(Long messageId, Long editorUserId, String newContent) {
		if (newContent == null || newContent.isBlank()) {
			throw new IllegalArgumentException("Nội dung trống");
		}
		Message msg = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));
		if (!msg.getSender().getUserId().equals(editorUserId)) {
			throw new RuntimeException("Chỉ người gửi mới được sửa tin nhắn");
		}
		if (Boolean.TRUE.equals(msg.getDeleted())) {
			throw new RuntimeException("Tin nhắn đã bị xóa");
		}
		msg.setContent(newContent);
		msg.setEditedAt(LocalDateTime.now());
		return messageRepository.save(msg);
	}

	@Override
	public void softDeleteMessage(Long messageId, Long requesterUserId) {
		Message msg = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));
		if (!msg.getSender().getUserId().equals(requesterUserId)) {
			throw new RuntimeException("Chỉ người gửi mới được xóa tin nhắn");
		}
		msg.setDeleted(true);
		msg.setContent("(đã xóa)");
		messageRepository.save(msg);
	}

	@Override
	public Message markDelivered(Long messageId, Long userId) {
		Message m = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));
		m.setStatus("DELIVERED");
		return messageRepository.save(m);
	}

	@Override
	public Message markSeen(Long messageId, Long userId) {
		Message m = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));
		m.setStatus("SEEN");
		return messageRepository.save(m);
	}

	@Override
	public Message sendMedia(Long roomId, Long senderId, String fileName, String contentType, String url) {
		ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
		User sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
		Message msg = new Message(room, sender, "");
		msg.setMediaFileName(fileName);
		msg.setMediaContentType(contentType);
		msg.setMediaUrl(url);
		return messageRepository.save(msg);
	}
}


