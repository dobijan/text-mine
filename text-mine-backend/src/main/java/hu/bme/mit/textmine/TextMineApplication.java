package hu.bme.mit.textmine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({
        "classpath*:configuration/spring.application.context.xml"
})
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class, DataSourceAutoConfiguration.class,
        MultipartAutoConfiguration.class, ValidationAutoConfiguration.class
})
public class TextMineApplication extends SpringBootServletInitializer {

    // public static void main(String[] args) {
    // SpringApplication.run(TextMineApplication.class, args);
    // }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TextMineApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TextMineApplication.class, args);
    }
}
