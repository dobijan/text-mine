package hu.bme.mit.textmine.mongo.document.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentFileDTO {
    private String author;
    private String title;
    private MultipartFile file;
    private String corpusId;
}
