package project.movie24;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import project.movie24.event.service.EventService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final EventService eventService;

    @GetMapping(value = "/")
    public String home(Model model) {
        model.addAttribute("homeEvents", eventService.findAll());
        return "home";
    }

}
