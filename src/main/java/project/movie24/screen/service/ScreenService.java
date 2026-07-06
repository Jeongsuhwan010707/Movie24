package project.movie24.screen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.dto.ScreenRequest;
import project.movie24.screen.repository.ScreenRepository;
import project.movie24.theater.domain.Theater;
import project.movie24.theater.service.TheaterService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterService theaterService;

    public Screen register(ScreenRequest request) {
        Theater theater = theaterService.findOne(request.getTheaterId());
        Screen screen = Screen.builder()
                .theater(theater)
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .screenType(request.getScreenType())
                .build();
        return screenRepository.save(screen);
    }

    public Screen update(Long screenId, ScreenRequest request) {
        Screen screen = getOrThrow(screenId);
        Theater theater = theaterService.findOne(request.getTheaterId());
        screen.update(theater, request.getName(), request.getTotalSeats(), request.getScreenType());
        return screen;
    }

    public void delete(Long screenId) {
        screenRepository.delete(getOrThrow(screenId));
    }

    @Transactional(readOnly = true)
    public Screen findOne(Long screenId) {
        return getOrThrow(screenId);
    }

    @Transactional(readOnly = true)
    public List<Screen> findAll() {
        return screenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Screen> findByTheaterId(Long theaterId) {
        return screenRepository.findByTheaterId(theaterId);
    }

    private Screen getOrThrow(Long screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. id=" + screenId));
    }
}
