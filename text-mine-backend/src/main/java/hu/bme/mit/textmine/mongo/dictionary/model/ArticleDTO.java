package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleDTO extends BaseMongoEntityDTO {

    private String documentId;

    private String entryWord;

    private Boolean properNoun;

    private Boolean derivative;

    private String editorNote;

    private String meaning;

    private List<PartOfSpeech> partOfSpeech;

    private List<String> externalReferences;

    private List<String> internalReferences;

    private List<FormVariant> formVariants;

    public static ArticleDTO from(Article article) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtils.copyProperties(article, dto);
        dto.setDocumentId(article.getDocumentId());
        return dto;
    }

    public static List<ArticleDTO> from(List<Article> articles) {
        return articles.stream().map(ArticleDTO::from).collect(Collectors.toList());
    }
}
