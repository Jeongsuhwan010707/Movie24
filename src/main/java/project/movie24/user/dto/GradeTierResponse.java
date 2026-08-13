package project.movie24.user.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.user.domain.Grade;

@Getter
@Builder
public class GradeTierResponse {

    private Grade grade;
    private int discountRate;

    // 이 등급이 되기 위한 최근 1년 누적 결제금액 기준선. NORMAL은 0(기본 등급).
    private int minSpend;
}
