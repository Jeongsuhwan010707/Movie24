package project.movie24.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FindIdResponse {
    private List<String> maskedLoginIds;
    private List<String> socialProviders;
}
