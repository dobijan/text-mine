package hu.bme.mit.textmine.mongo.dictionary.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleFileDTO {
    private String documentId;
    private MultipartFile file;
}
