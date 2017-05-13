package hu.bme.mit.textmine;

import org.apache.catalina.webresources.StandardRoot;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TomcatCachingConfig {

    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory() {
        TomcatEmbeddedServletContainerFactory tomcatFactory = new TomcatEmbeddedServletContainerFactory();
        tomcatFactory.addContextCustomizers((context) -> {
            StandardRoot standardRoot = new StandardRoot(context);
            standardRoot.setCacheMaxSize(40 * 1024);
            context.setResources(standardRoot);
            log.info(String.format("New cache size (KB): %d", context.getResources().getCacheMaxSize()));
        });
        return tomcatFactory;
    }
}
