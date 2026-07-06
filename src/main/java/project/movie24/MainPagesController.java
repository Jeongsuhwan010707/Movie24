package project.movie24;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPagesController {

    @GetMapping("/main/event")
    public String event() {
        return "main/event";
    }

    @GetMapping("/main/event2")
    public String event2() {
        return "main/event2";
    }

    @GetMapping("/main/movie")
    public String movie() {
        return "main/movie";
    }
}
