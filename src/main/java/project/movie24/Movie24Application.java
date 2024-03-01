package project.movie24;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

@SpringBootApplication
public class Movie24Application {

	public static void main(String[] args) {
		SpringApplication.run(Movie24Application.class, args);
	}
}
