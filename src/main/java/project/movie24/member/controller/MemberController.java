package project.movie24.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/users")
@Controller
public class MemberController {

    @GetMapping
    public String loginForm(){
        return "/users/loginForm";
    }

    @GetMapping("/terms")
    public String joinForm(){
        return "/users/terms";
    }

    @GetMapping("/authentication")
    public String authenticationForm(){
        return "/users/authentication";
    }

    @GetMapping("/new")
    public String createForm(){
        return "/users/userForm";
    }

    @GetMapping("/complete")
    public String completeForm(){
        return "/users/complete";
    }
}
