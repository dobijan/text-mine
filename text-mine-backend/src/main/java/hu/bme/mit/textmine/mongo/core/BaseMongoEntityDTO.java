package hu.bme.mit.textmine.mongo.core;

import java.util.Date;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseMongoEntityDTO {

    protected ObjectId id;

    protected Long version;

    protected Date createdDate;

    protected Date lastModifiedDate;

    protected String createdBy;

    protected String lastModifiedBy;
}
