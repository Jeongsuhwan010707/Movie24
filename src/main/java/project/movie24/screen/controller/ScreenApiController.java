package project.movie24.screen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.screen.domain.Screen;
import project.movie24.screen.dto.ScreenRequest;
import project.movie24.screen.dto.ScreenResponse;
import project.movie24.screen.service.ScreenService;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenApiController {

    private final ScreenService screenService;

    @PostMapping
    public ResponseEntity<ScreenResponse> register(@Valid @RequestBody ScreenRequest request) {
        Screen screen = screenService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ScreenResponse.from(screen));
    }

    @GetMapping
    public ResponseEntity<List<ScreenResponse>> list(@RequestParam(required = false) Long theaterId) {
        List<Screen> screens = theaterId == null ? screenService.findAll() : screenService.findByTheaterId(theaterId);
        return ResponseEntity.ok(screens.stream().map(ScreenResponse::from).toList());
    }

    @GetMapping("/{screenId}")
    public ResponseEntity<ScreenResponse> findOne(@PathVariable Long screenId) {
        return ResponseEntity.ok(ScreenResponse.from(screenService.findOne(screenId)));
    }

    @PutMapping("/{screenId}")
    public ResponseEntity<ScreenResponse> update(@PathVariable Long screenId, @Valid @RequestBody ScreenRequest request) {
        Screen screen = screenService.update(screenId, request);
        return ResponseEntity.ok(ScreenResponse.from(screen));
    }

    @DeleteMapping("/{screenId}")
    public ResponseEntity<Void> delete(@PathVariable Long screenId) {
        screenService.delete(screenId);
        return ResponseEntity.noContent().build();
    }
}
