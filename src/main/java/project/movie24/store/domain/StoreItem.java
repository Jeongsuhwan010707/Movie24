package project.movie24.store.domain;

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

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StoreItem {

    @Id @GeneratedValue
    @Column(name = "store_item_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private StoreCategory category;

    private String name;
    private String description;
    private Integer price;
    private String imageUrl;

    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(StoreCategory category, String name, String description, Integer price,
                        String imageUrl, Integer displayOrder) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}
