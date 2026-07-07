package project.movie24.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StoreController {

    @GetMapping("/store")
    public String index() {
        return "store/index";
    }

    @GetMapping("/store/bag")
    public String bag() {
        return "store/bag";
    }

    @GetMapping("/store/bagPay")
    public String bagPay() {
        return "store/bagPay";
    }

    @GetMapping("/store/payDone")
    public String payDone() {
        return "store/payDone";
    }
}
