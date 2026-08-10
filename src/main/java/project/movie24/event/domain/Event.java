package project.movie24.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id @GeneratedValue
    @Column(name = "event_id")
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Builder.Default
    private Boolean winnerAnnounced = false;

    @Column(columnDefinition = "TEXT")
    private String winnerContent;

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(String title, String content, String thumbnailUrl, EventStatus status,
                        Boolean winnerAnnounced, String winnerContent,
                        LocalDate startDate, LocalDate endDate, Integer displayOrder) {
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.winnerAnnounced = winnerAnnounced != null ? winnerAnnounced : false;
        this.winnerContent = winnerContent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }
}
