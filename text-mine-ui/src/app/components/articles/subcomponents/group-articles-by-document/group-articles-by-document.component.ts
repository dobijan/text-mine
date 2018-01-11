import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ArticleService } from '../../../../services/article-service/article.service';
import { ArticleDTO } from '../../../../services/api/generated/model/ArticleDTO';
import { DocumentService } from './../../../../services/document-service/document.service';
import { CorpusService } from './../../../../services/corpus-service/corpus.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Corpus } from '../../../../services/api/generated/model/Corpus';

@Component({
    selector: 'app-group-articles-by-document',
    templateUrl: './group-articles-by-document.component.html',
    styleUrls: ['./group-articles-by-document.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class GroupArticlesByDocumentComponent implements OnInit {

    title: String = 'Filter Articles';

    documents: Array<Document> = [];

    corpora: Array<Corpus> = [];

    documentTitles: Map<string, string> = new Map();

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

    groups: Array<{
        id: string,
        articles: Array<ArticleDTO>
    }> = [];

    filters = {
        entryWord: null,
        formVariant: null,
        inflection: null,
        pos: undefined,
        corpusId: undefined,
        matchingStrategy: undefined,
        posCount: null
    };

    constructor(
        private articleService: ArticleService,
        private documentService: DocumentService,
        private corpusService: CorpusService
    ) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => {
            this.documents = res;
            for (const document of this.documents) {
                this.documentTitles.set(<string>document.id, document.title);
            }
        });
        console.log(this.documentTitles);
        this.corpusService.getCorpora().subscribe(res => this.corpora = res);
    }

    onSubmit() {
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.groups = null;
        this.articleService.getGroupedByDocument(this.filters.entryWord, this.filters.formVariant,
            this.filters.inflection, this.filters.pos, this.filters.corpusId, this.filters.matchingStrategy,
            this.filters.posCount).subscribe(res => {
                this.groups = [];
                for (const key of Object.keys(res)) {
                    const group = {
                        id: key,
                        articles: res[key] as any as Array<ArticleDTO>
                    };
                    this.groups.push(group);
                }
                this.progressMode = 'determinate';
                this.progressValue = 100;
            },
                err => {
                    console.log('Error loading Article Groups!');
                    this.progressMode = 'determinate';
                    this.progressValue = 0;
                });
    }
}
