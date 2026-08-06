package project.movie24.theater.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Theater {

    @Id @GeneratedValue
    @Column(name = "theater_id")
    private Long id;

    private String name;
    private String region;
    private String address;
    private Double latitude;
    private Double longitude;

    public void update(String name, String region, String address, Double latitude, Double longitude) {
        this.name = name;
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
