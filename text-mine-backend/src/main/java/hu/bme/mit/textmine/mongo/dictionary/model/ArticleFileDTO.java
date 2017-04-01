package hu.bme.mit.textmine.mongo.dictionary.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleFileDTO {
    private String documentId;
    private MultipartFile file;
}
