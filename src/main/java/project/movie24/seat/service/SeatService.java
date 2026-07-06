package project.movie24.seat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.service.ScreenService;
import project.movie24.seat.domain.Seat;
import project.movie24.seat.dto.SeatGenerateRequest;
import project.movie24.seat.dto.SeatRequest;
import project.movie24.seat.repository.SeatRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenService screenService;

    public Seat register(SeatRequest request) {
        Screen screen = screenService.findOne(request.getScreenId());
        Seat seat = Seat.builder()
                .screen(screen)
                .rowLabel(request.getRowLabel())
                .seatNumber(request.getSeatNumber())
                .build();
        return seatRepository.save(seat);
    }

    public List<Seat> generate(SeatGenerateRequest request) {
        Screen screen = screenService.findOne(request.getScreenId());
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < request.getRowCount(); row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int seatNumber = 1; seatNumber <= request.getSeatsPerRow(); seatNumber++) {
                seats.add(Seat.builder()
                        .screen(screen)
                        .rowLabel(rowLabel)
                        .seatNumber(seatNumber)
                        .build());
            }
        }
        return seatRepository.saveAll(seats);
    }

    public void delete(Long seatId) {
        seatRepository.delete(getOrThrow(seatId));
    }

    @Transactional(readOnly = true)
    public List<Seat> findByScreenId(Long screenId) {
        return seatRepository.findByScreenId(screenId);
    }

    @Transactional(readOnly = true)
    public List<Seat> findAllByIds(List<Long> seatIds) {
        return seatRepository.findAllById(seatIds);
    }

    private Seat getOrThrow(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다. id=" + seatId));
    }
}
