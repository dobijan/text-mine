package hu.bme.mit.textmine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:configuration/spring.application.context.xml"})
public class TextMineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextMineApplication.class, args);
	}
}
