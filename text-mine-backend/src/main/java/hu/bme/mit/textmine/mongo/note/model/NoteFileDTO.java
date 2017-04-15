package hu.bme.mit.textmine.mongo.note.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteFileDTO {

    private String documentId;
    private MultipartFile file;
}
