package project.movie24.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import project.movie24.user.domain.EmailStatus;
import project.movie24.user.domain.User;
import project.movie24.user.domain.UserForm;
import project.movie24.user.service.UserService;

@RequestMapping("/users")
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/terms")
    public String joinForm(){
        return "/users/terms";
    }

    @GetMapping("/authentication")
    public String authenticationForm(){
        return "/users/authentication";
    }

    @GetMapping("/new")
    public String createForm(Model model){
        model.addAttribute("user", new User());
        return "/users/userForm";
    }

    @PostMapping("/new")
    public String create(UserForm userForm){
        if ("Y".equals(userForm.getEmailYn())) {
            userForm.setEmailStatus(EmailStatus.ALLOW);
        } else {
            userForm.setEmailStatus(EmailStatus.REJECT);
        }

        User user = User.builder()
                .loginId(userForm.getLoginId())
                .password(userForm.getPassword())
                .name(userForm.getName())
                .nickName(userForm.getNickName())
                .address(userForm.getAddress())
                .phone(userForm.getPhone())
                .email(userForm.getEmail())
                .emailStatus(userForm.getEmailStatus())
                .gender(userForm.getGender())
                .birthDate(userForm.getBirthDate())
                .build();

        userService.saveUser(user);
        return "redirect:/users/complete";
    }

    @GetMapping("/complete")
    public String completeForm(){
        return "/users/complete";
    }
}
