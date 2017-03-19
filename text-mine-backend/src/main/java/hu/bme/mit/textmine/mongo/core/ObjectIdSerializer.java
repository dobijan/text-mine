package hu.bme.mit.textmine.mongo.core;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ObjectIdSerializer extends JsonSerializer<ObjectId> {

	@Override
	public void serialize(ObjectId anObjectId, JsonGenerator aJsonGenerator, SerializerProvider aProvider)
			throws IOException, JsonProcessingException {
		if (anObjectId == null) {
			aJsonGenerator.writeNull();
		} else {
			aJsonGenerator.writeString(anObjectId.toString());
		}
	}

}
