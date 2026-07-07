package project.movie24.theater.dto;

import lombok.Builder;
import lombok.Getter;
import project.movie24.theater.domain.Theater;

@Getter
@Builder
public class TheaterResponse {

    private Long id;
    private String name;
    private String region;
    private String address;

    public static TheaterResponse from(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .region(theater.getRegion())
                .address(theater.getAddress())
                .build();
    }
}
