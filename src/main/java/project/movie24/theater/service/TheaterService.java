package project.movie24.theater.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.movie24.theater.domain.Theater;
import project.movie24.theater.dto.TheaterRequest;
import project.movie24.theater.repository.TheaterRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public Theater register(TheaterRequest request) {
        Theater theater = Theater.builder()
                .name(request.getName())
                .region(request.getRegion())
                .address(request.getAddress())
                .build();
        return theaterRepository.save(theater);
    }

    public Theater update(Long theaterId, TheaterRequest request) {
        Theater theater = getOrThrow(theaterId);
        theater.update(request.getName(), request.getRegion(), request.getAddress());
        return theater;
    }

    public void delete(Long theaterId) {
        theaterRepository.delete(getOrThrow(theaterId));
    }

    @Transactional(readOnly = true)
    public Theater findOne(Long theaterId) {
        return getOrThrow(theaterId);
    }

    @Transactional(readOnly = true)
    public List<Theater> findAll() {
        return theaterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Theater> findByRegion(String region) {
        return theaterRepository.findByRegion(region);
    }

    private Theater getOrThrow(Long theaterId) {
        return theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 극장입니다. id=" + theaterId));
    }
}
