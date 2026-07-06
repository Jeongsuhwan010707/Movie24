package project.movie24.help.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelpController {

    @GetMapping("/help")
    public String index() {
        return "help/index";
    }

    @GetMapping("/help/post")
    public String post() {
        return "help/post";
    }

    @GetMapping("/help/postInfo")
    public String postInfo() {
        return "help/postInfo";
    }

    @GetMapping("/help/postInsert")
    public String postInsert() {
        return "help/postInsert";
    }
}
