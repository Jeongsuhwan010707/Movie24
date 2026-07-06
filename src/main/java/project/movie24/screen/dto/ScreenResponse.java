package project.movie24.screen.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.screen.domain.Screen;

@Getter
@Builder
public class ScreenResponse {

    private Long id;
    private Long theaterId;
    private String theaterName;
    private String name;
    private Integer totalSeats;
    private String screenType;

    public static ScreenResponse from(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .theaterId(screen.getTheater().getId())
                .theaterName(screen.getTheater().getName())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .screenType(screen.getScreenType())
                .build();
    }
}
