package QuanLy.Chat.Service.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import QuanLy.Chat.Entity.Call;
import QuanLy.Chat.Repository.CallRepository;
import QuanLy.Chat.Service.CallService;

@Service
public class CallServiceImpl implements CallService {

	private final CallRepository callRepository;

	public CallServiceImpl(CallRepository callRepository) {
		this.callRepository = callRepository;
	}

	@Override
	public Call startCall(Long callerId, Long receiverId, String callType) {
		if (callerId == null || receiverId == null) {
			throw new IllegalArgumentException("Thiếu tham số người gọi/nhận");
		}
		Call call = new Call();
		call.setCallerId(callerId);
		call.setReceiverId(receiverId);
		call.setCallType(callType == null ? "VOICE" : callType);
		call.setStartTime(LocalDateTime.now());
		call.setStatus("ONGOING");
		return callRepository.save(call);
	}

	@Override
	public Call endCall(Long callId) {
		Call call = callRepository.findById(callId).orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc gọi"));
		call.setEndTime(LocalDateTime.now());
		call.setStatus("ENDED");
		return callRepository.save(call);
	}

	@Override
	public List<Call> listCallsOfUser(Long userId) {
		return callRepository.findByCallerIdOrReceiverId(userId, userId);
	}

	@Override
	public void deleteCall(Long callId) {
		callRepository.deleteById(callId);
	}

	@Override
	public int deleteCallsInRange(LocalDateTime from, LocalDateTime to) {
		List<Call> all = callRepository.findAll();
		int before = all.size();
		all.stream()
			.filter(c -> c.getStartTime() != null && !c.getStartTime().isBefore(from) && !c.getStartTime().isAfter(to))
			.forEach(c -> callRepository.deleteById(c.getId()));
		int after = callRepository.findAll().size();
		return before - after;
	}
}


