package project.movie24.theater.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TheaterController {

    @Value("${kakao.map.js-key}")
    private String kakaoMapJsKey;

    @GetMapping("/theaters/nearby")
    public String nearby(Model model) {
        model.addAttribute("kakaoMapJsKey", kakaoMapJsKey);
        return "theater/nearby";
    }
}
