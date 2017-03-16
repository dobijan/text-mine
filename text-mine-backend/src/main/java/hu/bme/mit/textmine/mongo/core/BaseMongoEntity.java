package hu.bme.mit.textmine.mongo.core;

import java.io.Serializable;
import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BaseMongoEntity implements Serializable {
	private static final long serialVersionUID = 7035851559619057861L;

	@Id
	protected ObjectId id;

	@Version
	protected Long version;

	@CreatedDate
	protected Date createdDate;

	@LastModifiedDate
	protected Date lastModifiedDate;

	@CreatedBy
	protected String createdBy;

	@LastModifiedBy
	protected String lastModifiedBy;
}
