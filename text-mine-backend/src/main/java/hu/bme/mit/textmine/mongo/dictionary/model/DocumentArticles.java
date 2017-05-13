package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentArticles {

    private Object document;
    private List<Article> articles;
}
