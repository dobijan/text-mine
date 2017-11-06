package hu.bme.mit.textmine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

//@WebAppConfiguration
// @ComponentScan(basePackageClasses = {
// DocumentController.class,
// CorpusController.class,
// ArticleController.class,
// NoteController.class,
// LocationController.class,
// PersonController.class
// })
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("hu.bme.mit.textmine"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Text Mine REST API Documentation",
                "This is a Swagger Documentation of the Text Mine API, generated with Springfox.",
                "N/A",
                "Terms of service not applicable.",
                new Contact("Dobi Jan Sándor", "https://github.com/dobijan", "dobijan@hotmail.com"),
                "Mozilla Public License", "https://www.mozilla.org/en-US/MPL/2.0/");
    }
}
