package project.movie24.movie.domain;

public enum AgeRating {
    ALL, AGE_12, AGE_15, AGE_18;

    public String getImageFileName() {
        return switch (this) {
            case ALL -> "전체관람가.png";
            case AGE_12 -> "12세이상관람가.png";
            case AGE_15 -> "15세이상관람가.png";
            case AGE_18 -> "18세이상관람가.png";
        };
    }
}
