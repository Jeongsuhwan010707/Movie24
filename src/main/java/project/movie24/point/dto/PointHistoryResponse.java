package project.movie24.point.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.point.domain.PointHistory;
import project.movie24.point.domain.PointHistoryType;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointHistoryResponse {

    private PointHistoryType type;
    private Integer changeAmount;
    private Integer balanceAfter;
    private LocalDateTime createdAt;

    public static PointHistoryResponse from(PointHistory history) {
        return PointHistoryResponse.builder()
                .type(history.getType())
                .changeAmount(history.getChangeAmount())
                .balanceAfter(history.getBalanceAfter())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
