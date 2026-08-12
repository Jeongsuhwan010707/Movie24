package project.movie24.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.point.domain.PointHistory;
import project.movie24.point.domain.PointHistoryType;
import project.movie24.point.dto.PointHistoryResponse;
import project.movie24.point.repository.PointHistoryRepository;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public User earn(User user, int amount, Long reservationId) {
        if (amount <= 0) {
            return user;
        }
        return applyChange(user, amount, PointHistoryType.EARN, reservationId);
    }

    public User use(User user, int amount, Long reservationId) {
        if (amount <= 0) {
            return user;
        }
        if (user.getPoint() < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        return applyChange(user, -amount, PointHistoryType.USE, reservationId);
    }

    /**
     * 예매 취소로 인한 적립 포인트 회수. 이미 다른 곳에 써버려 잔액이 부족하더라도
     * 마이너스로 만들지 않고 0까지만 회수한다.
     */
    public User cancelEarn(User user, int amount, Long reservationId) {
        if (amount <= 0) {
            return user;
        }
        int recovered = Math.min(amount, user.getPoint());
        return applyChange(user, -recovered, PointHistoryType.CANCEL_EARN, reservationId);
    }

    public User cancelUse(User user, int amount, Long reservationId) {
        if (amount <= 0) {
            return user;
        }
        return applyChange(user, amount, PointHistoryType.CANCEL_USE, reservationId);
    }

    @Transactional(readOnly = true)
    public List<PointHistoryResponse> findHistory(Long userId) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PointHistoryResponse::from)
                .toList();
    }

    private User applyChange(User user, int changeAmount, PointHistoryType type, Long reservationId) {
        int balanceAfter = user.getPoint() + changeAmount;
        User updated = userRepository.save(user.toBuilder().point(balanceAfter).build());

        pointHistoryRepository.save(PointHistory.builder()
                .user(updated)
                .reservationId(reservationId)
                .type(type)
                .changeAmount(changeAmount)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build());

        return updated;
    }
}
