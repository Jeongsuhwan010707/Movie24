package project.movie24.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.movie24.store.domain.TicketVoucher;
import project.movie24.store.domain.TicketVoucherStatus;

import java.util.List;
import java.util.Optional;

public interface TicketVoucherRepository extends JpaRepository<TicketVoucher, Long> {

    List<TicketVoucher> findByUser_IdAndStatusOrderByIssuedAtAsc(Long userId, TicketVoucherStatus status);

    Optional<TicketVoucher> findByIdAndUser_Id(Long ticketVoucherId, Long userId);

    Optional<TicketVoucher> findByReservationId(Long reservationId);
}
