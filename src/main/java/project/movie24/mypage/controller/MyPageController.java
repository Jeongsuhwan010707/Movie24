package project.movie24.mypage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageController {

    @GetMapping("/myPage")
    public String index() {
        return "myPage/index";
    }
}
