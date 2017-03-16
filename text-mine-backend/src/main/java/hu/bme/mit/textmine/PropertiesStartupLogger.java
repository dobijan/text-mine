package hu.bme.mit.textmine;

import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PropertiesStartupLogger {
	private static Logger logger = LoggerFactory.getLogger(PropertiesStartupLogger.class);

	  @Resource(name = "applicationProperties")
	  private Properties props;

	  @PostConstruct
	  public void init() {
		  logger.info("Properties Startup Logger:");
		  for (Map.Entry<Object, Object> prop : props.entrySet()) {
			  logger.info("{}={}", prop.getKey(), prop.getValue());
		  }
	  }
}