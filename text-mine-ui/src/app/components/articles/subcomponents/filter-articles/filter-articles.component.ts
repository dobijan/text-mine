import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ArticleService } from '../../../../services/article-service/article.service';
import { ArticleDTO } from '../../../../services/api/generated/model/ArticleDTO';
import { DocumentService } from './../../../../services/document-service/document.service';
import { CorpusService } from './../../../../services/corpus-service/corpus.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Corpus } from '../../../../services/api/generated/model/Corpus';

@Component({
  selector: 'app-filter-articles',
  templateUrl: './filter-articles.component.html',
  styleUrls: ['./filter-articles.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class FilterArticlesComponent implements OnInit {

    title: String = 'Filter Articles';

    documents: Array<Document> = [];

    corpora: Array<Corpus> = [];

    strategies: Array<string> = [
        'EXACT_MATCH',
        'STARTS_WITH',
        'ENDS_WITH',
        'CONTAINS'
    ];

    poss: Array<ArticleDTO.PartOfSpeechEnum> = [
        ArticleDTO.PartOfSpeechEnum.PRONOMINALADVERB,
        ArticleDTO.PartOfSpeechEnum.PRONOUN,
        ArticleDTO.PartOfSpeechEnum.NOUN,
        ArticleDTO.PartOfSpeechEnum.INFINITIVE,
        ArticleDTO.PartOfSpeechEnum.ADVERBIALPARTICIPLE,
        ArticleDTO.PartOfSpeechEnum.ARTICLE,
        ArticleDTO.PartOfSpeechEnum.ADVERB,
        ArticleDTO.PartOfSpeechEnum.NUMERAL,
        ArticleDTO.PartOfSpeechEnum.VERB,
        ArticleDTO.PartOfSpeechEnum.VERBPARTICIPLE,
        ArticleDTO.PartOfSpeechEnum.PREVERB,
        ArticleDTO.PartOfSpeechEnum.INTERJECTION,
        ArticleDTO.PartOfSpeechEnum.CONJUNCTIVE,
        ArticleDTO.PartOfSpeechEnum.ADJECTIVE,
        ArticleDTO.PartOfSpeechEnum.PARTICIPLE,
        ArticleDTO.PartOfSpeechEnum.MODIFIER,
        ArticleDTO.PartOfSpeechEnum.SENTENTIAL,
        ArticleDTO.PartOfSpeechEnum.POSTPOSITION,
        ArticleDTO.PartOfSpeechEnum.POSTPOSITIONALADJECTIVE,
        ArticleDTO.PartOfSpeechEnum.PARTICLE,
        ArticleDTO.PartOfSpeechEnum.AUXILIARY
    ];

    progressMode: String = 'determinate';

    progressValue = 0;

    articles: Array<ArticleDTO> = [];

    filters = {
        entryWord: null,
        formVariant: null,
        inflection: null,
        pos: undefined,
        corpusId: undefined,
        documentId: undefined,
        matchingStrategy: undefined,
        offset: null,
        limit: null
    };

    constructor(
        private articleService: ArticleService,
        private documentService: DocumentService,
        private corpusService: CorpusService
    ) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
        this.corpusService.getCorpora().subscribe(res => this.corpora = res);
    }

    onSubmit() {
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.articles = null;
        this.articleService.getFiltered(this.filters.entryWord, this.filters.formVariant, this.filters.inflection,
            this.filters.pos, this.filters.documentId, this.filters.corpusId, this.filters.matchingStrategy,
            this.filters.offset, this.filters.limit).subscribe(res => {
                this.articles = res;
                this.progressMode = 'determinate';
                this.progressValue = 100;
            },
                err => {
                    console.log('Error loading Articles!');
                    this.progressMode = 'determinate';
                    this.progressValue = 0;
                });
    }

}
