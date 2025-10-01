package QuanLy.Chat.Service;

import java.util.List;

import QuanLy.Chat.Entity.Call;

public interface CallService {
	Call startCall(Long callerId, Long receiverId, String callType);
	Call endCall(Long callId);
	List<Call> listCallsOfUser(Long userId);
	void deleteCall(Long callId);
	int deleteCallsInRange(java.time.LocalDateTime from, java.time.LocalDateTime to);
}


