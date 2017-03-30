package hu.bme.mit.textmine.mongo.dictionary.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.dictionary.dal.ArticleRepository;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleFileDTO;
import hu.bme.mit.textmine.mongo.dictionary.model.EntryExample;
import hu.bme.mit.textmine.mongo.dictionary.model.FormVariant;
import hu.bme.mit.textmine.mongo.dictionary.model.Inflection;
import hu.bme.mit.textmine.mongo.dictionary.model.QArticle;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;

@Service
public class ArticleService {

    private static final Pattern
        entryWordPattern = Pattern.compile("^.*<U>(.*)</U>.*$"),
        internalReferencPattern = Pattern.compile("^.*<R>(.*)</R>.*$"),
        externalReferencPattern = Pattern.compile("^.*<RT>(.*)</RT>.*$"),
        properNounPattern = Pattern.compile("^.*<T_/>.*$"),
        derivativePattern = Pattern.compile("^.*<KT_/>.*$"),
        editorNotePattern = Pattern.compile("^.*<K>(.*)</K>.*$"),
        meaningPattern = Pattern.compile("^.*<J>(.*)</J>.*$"),
        formVariantPattern = Pattern.compile("^.*<Q>(.*)</Q>.*$"),
        inflectionPattern = Pattern.compile("^.*<B>(.*)</B>( – )?(\\d+)?.*$"),
        occurrencePattern = Pattern.compile("^.*(.*)<I>(.*)</I>(.*) \\(TL (\\d+)\\).*$");
        
    @Autowired
    private ArticleRepository repository;
    
    @Autowired
    private DocumentService documentService;
    
    public boolean exists(String id) {
        Long count = this.repository.count(new QArticle("article").id.eq(new ObjectId(id)));
        return count > 0;
    }
    
    public Article getArticle(String id) {
        return this.repository.findOne(id);
    }
    
    public List<Article> getAll() {
        return this.repository.findAll();
    }
    
    public List<Article> getArticlesByDocument(String id) {
        Document document = this.documentService.getDocument(id);
        if (document == null) {
            return null;
        }
        return this.repository.findByDocumentId(new ObjectId(id));
    }
    
    public Article createArticle(ArticleFileDTO dto) throws IOException {
        Document document = this.documentService.getDocument(dto.getDocumentId());
        if (document == null) {
            return null;
        }
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        List<String> lines = Arrays.asList(content.split("\\R+"));
        Article article = new Article();
        article.setDocument(document);
        article.setProperNoun(false);
        article.setDerivative(false);
        article.setInternalReferences(Lists.newArrayList());
        article.setExternalReferences(Lists.newArrayList());
        article.setFormVariants(Lists.newArrayList());
        
        FormVariant currentFormVariant = null;
        Inflection currentInflection = null;
        
        for (String line : lines) {
            // find article entry word
            Matcher m = entryWordPattern.matcher(line);
            if (m.matches()) {
                article.setEntryWord(m.group(1));
                continue;
            }
            // find if proper noun
            m = properNounPattern.matcher(line);
            if (m.matches()) {
                article.setProperNoun(true);
                continue;
            }
            // find if derivative
            m = derivativePattern.matcher(line);
            if (m.matches()) {
                article.setDerivative(true);
                continue;
            }
            // find a reference
            m = internalReferencPattern.matcher(line);
            if (m.matches()) {
                article.getInternalReferences().add(m.group(1));
                continue;
            }
            // find an external reference
            m = externalReferencPattern.matcher(line);
            if (m.matches()) {
                article.getExternalReferences().add(m.group(1));
                continue;
            }
            // find editor note
            m = editorNotePattern.matcher(line);
            if (m.matches()) {
                article.setEditorNote(m.group(1));
                continue;
            }
            // find meaning (of life)
            m = meaningPattern.matcher(line);
            if (m.matches()) {
                article.setMeaning(m.group(1));
                continue;
            }
            // find a form variant header
            m = formVariantPattern.matcher(line);
            if (m.matches()) {
                FormVariant formVariant = new FormVariant();
                formVariant.setName(m.group(1));
                formVariant.setInflections(Lists.newArrayList());
                article.getFormVariants().add(formVariant);
                currentFormVariant = formVariant;
                continue;
            }
            // find an inflection
            m = inflectionPattern.matcher(line);
            if (m.matches()) {
                Inflection inflection = new Inflection();
                inflection.setName(m.group(1));
                String occurrences = m.group(3);
                if (occurrences != null) {                    
                    inflection.setOccurrences(Integer.parseInt(occurrences));
                }
                inflection.setExamples(Lists.newArrayList());
                currentInflection = inflection;
                currentFormVariant.getInflections().add(inflection);
            }
            // find an occurrence
            m = occurrencePattern.matcher(line);
            if (m.matches()) {
                EntryExample example = new EntryExample();
                example.setExampleSentence(m.group(1) + m.group(2) + m.group(3));
                example.setPage(Integer.parseInt(m.group(4)));
                currentInflection.getExamples().add(example);
            }
        }
        return this.repository.insert(article);
    }
    
    public Article updateArticle(Article article) {
        this.repository.save(article);
        return article;
    }
    
    public void removeArticle(String id) {
        this.repository.delete(id);
    }
    
    public void removeArticles(List<Article> articles) {
        this.repository.delete(articles);
    }
}
