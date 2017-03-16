package hu.bme.mit.textmine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:configuration/spring.application.context.xml"})
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        MultipartAutoConfiguration.class})
public class TextMineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextMineApplication.class, args);
	}
}
