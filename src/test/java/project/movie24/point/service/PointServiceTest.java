package project.movie24.point.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.movie24.point.domain.PointHistory;
import project.movie24.point.repository.PointHistoryRepository;
import project.movie24.user.domain.User;
import project.movie24.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Test
    void earn_addsPointsAndRecordsHistory() {
        PointService pointService = new PointService(userRepository, pointHistoryRepository);
        User user = User.builder().id(1L).point(1000).build();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = pointService.earn(user, 200, 10L);

        assertThat(updated.getPoint()).isEqualTo(1200);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @Test
    void use_throwsWhenBalanceInsufficient() {
        PointService pointService = new PointService(userRepository, pointHistoryRepository);
        User user = User.builder().id(1L).point(100).build();

        assertThatThrownBy(() -> pointService.use(user, 200, 10L))
                .isInstanceOf(IllegalStateException.class);
        verify(pointHistoryRepository, never()).save(any(PointHistory.class));
    }

    @Test
    void use_deductsPointsWhenBalanceSufficient() {
        PointService pointService = new PointService(userRepository, pointHistoryRepository);
        User user = User.builder().id(1L).point(1000).build();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = pointService.use(user, 300, 10L);

        assertThat(updated.getPoint()).isEqualTo(700);
    }

    @Test
    void cancelEarn_clampsRecoveryAtZeroWhenBalanceAlreadySpent() {
        PointService pointService = new PointService(userRepository, pointHistoryRepository);
        User user = User.builder().id(1L).point(50).build();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = pointService.cancelEarn(user, 200, 10L);

        assertThat(updated.getPoint()).isEqualTo(0);
    }

    @Test
    void cancelUse_refundsPoints() {
        PointService pointService = new PointService(userRepository, pointHistoryRepository);
        User user = User.builder().id(1L).point(100).build();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = pointService.cancelUse(user, 300, 10L);

        assertThat(updated.getPoint()).isEqualTo(400);
    }
}
