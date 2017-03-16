package hu.bme.mit.textmine.mongo.core;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ObjectMapperConfigurator {

	@Bean
	public Jackson2ObjectMapperBuilder buildObjectIdMapper() {
		Jackson2ObjectMapperBuilder aBuilder = new Jackson2ObjectMapperBuilder();
		aBuilder.serializerByType(ObjectId.class, new ObjectIdSerializer());
		return aBuilder;
	}
}
