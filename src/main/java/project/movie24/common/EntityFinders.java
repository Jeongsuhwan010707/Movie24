package project.movie24.common;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 여러 feature 서비스가 똑같이 "id로 조회하고 없으면 IllegalArgumentException"을 반복하던 걸 한 곳으로 모은다.
 */
public final class EntityFinders {

    private EntityFinders() {
    }

    public static <T, ID> T getOrThrow(JpaRepository<T, ID> repository, ID id, String entityLabel) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 " + entityLabel + "입니다. id=" + id));
    }
}
