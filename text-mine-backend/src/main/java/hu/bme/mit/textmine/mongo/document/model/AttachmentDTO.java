package hu.bme.mit.textmine.mongo.document.model;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentDTO {

    private Map<String, String> metadata;
    private MultipartFile content;
}
