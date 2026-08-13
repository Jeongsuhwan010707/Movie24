package project.movie24.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.movie24.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StoreOrder {

    @Id @GeneratedValue
    @Column(name = "store_order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String orderUid;
    private Integer totalAmount;
    private LocalDateTime orderedAt;

    // 매장 키오스크에서 상품(팝콘 등)을 수령할 때 직접 입력하는 코드("XXXX-XXXX-XXXX-XXXX").
    // 주문 확정 시 한 번 생성되면 바뀌지 않는다. QR은 이 값을 그대로 인코딩한 것뿐이라, 온라인에서는
    // 확인용으로만 쓰고 실제 수령은 이 코드를 키오스크에 입력하는 방식을 기준으로 한다.
    private String entryCode;
}
