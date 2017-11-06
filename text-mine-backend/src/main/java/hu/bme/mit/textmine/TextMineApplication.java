package hu.bme.mit.textmine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ImportResource({
        "classpath*:configuration/spring.application.context.xml"
})
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class, HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class,
        MultipartAutoConfiguration.class, ValidationAutoConfiguration.class
})
@EnableSwagger2
public class TextMineApplication extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {

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

    // @Bean
    // public Docket petApi() {
    // return new Docket(DocumentationType.SWAGGER_2)
    // .select()
    // .apis(RequestHandlerSelectors.any())
    // .paths(PathSelectors.any())
    // .build()
    // .pathMapping("/")
    // .directModelSubstitute(LocalDate.class,
    // String.class)
    // .genericModelSubstitutes(ResponseEntity.class)
    // .alternateTypeRules(
    // newRule(typeResolver.resolve(DeferredResult.class,
    // typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
    // typeResolver.resolve(WildcardType.class)))
    // .useDefaultResponseMessages(false)
    // .globalResponseMessage(RequestMethod.GET,
    // newArrayList(new ResponseMessageBuilder()
    // .code(500)
    // .message("500 message")
    // .responseModel(new ModelRef("Error"))
    // .build()))
    // .securitySchemes(newArrayList(apiKey()))
    // .securityContexts(newArrayList(securityContext()))
    // .enableUrlTemplating(true)
    // .globalOperationParameters(
    // newArrayList(new ParameterBuilder()
    // .name("someGlobalParameter")
    // .description("Description of someGlobalParameter")
    // .modelRef(new ModelRef("string"))
    // .parameterType("query")
    // .required(true)
    // .build()))
    // .tags(new Tag("Pet Service", "All apis relating to pets"));
    // }
    //
    // @Autowired
    // private TypeResolver typeResolver;
    //
    // private ApiKey apiKey() {
    // return new ApiKey("mykey", "api_key", "header");
    // }
    //
    // private SecurityContext securityContext() {
    // return SecurityContext.builder()
    // .securityReferences(defaultAuth())
    // .forPaths(PathSelectors.regex("/anyPath.*"))
    // .build();
    // }
    //
    // List<SecurityReference> defaultAuth() {
    // AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    // AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    // authorizationScopes[0] = authorizationScope;
    // return newArrayList(
    // new SecurityReference("mykey", authorizationScopes));
    // }
    //
    // @Bean
    // SecurityConfiguration security() {
    // return new SecurityConfiguration(
    // "test-app-client-id",
    // "test-app-client-secret",
    // "test-app-realm",
    // "test-app",
    // "apiKey",
    // ApiKeyVehicle.HEADER,
    // "api_key",
    // "," /* scope separator */);
    // }
    //
    // @Bean
    // UiConfiguration uiConfig() {
    // return new UiConfiguration(
    // "validatorUrl", // url
    // "none", // docExpansion => none | list
    // "alpha", // apiSorter => alpha
    // "schema", // defaultModelRendering => schema
    // UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS,
    // false, // enableJsonEditor => true | false
    // true, // showRequestHeaders => true | false
    // 60000L); // requestTimeout => in milliseconds, defaults to null (uses jquery xh timeout)
    // }
}
