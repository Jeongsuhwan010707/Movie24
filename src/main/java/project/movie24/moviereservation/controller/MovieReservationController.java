package project.movie24.moviereservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MovieReservationController {

    @GetMapping("/movieReservation/time")
    public String time() {
        return "movieReservation/time";
    }

    @GetMapping("/movieReservation/seat")
    public String seat() {
        return "movieReservation/seat";
    }

    @GetMapping("/movieReservation/pay")
    public String pay() {
        return "movieReservation/pay";
    }

    @GetMapping("/movieReservation/payDone")
    public String payDone() {
        return "movieReservation/payDone";
    }
}
