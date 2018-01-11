import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { ArticleDTO } from '../../../../services/api/generated/model/ArticleDTO';


@Component({
    selector: 'app-filter-documents',
    templateUrl: './filter-documents.component.html',
    styleUrls: ['./filter-documents.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class FilterDocumentsComponent implements OnInit {

    title: String = 'Filter Documents';

    documents: Array<Document> = [];

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

    results: Array<Document> = [];

    filters = {
        entryText: null,
        entryWord: null,
        pos: undefined,
        documentId: undefined,
        offset: null,
        limit: null
    };

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.results = null;
        this.documentService.getFilteredDocuments(this.filters.entryText, this.filters.entryWord, this.filters.pos,
            this.filters.documentId, this.filters.offset, this.filters.limit).subscribe(res => {
                this.results = res;
                this.progressMode = 'determinate';
                this.progressValue = 100;
            },
            err => {
                console.log('Error loading Documents!');
                this.progressMode = 'determinate';
                this.progressValue = 0;
            });
    }
}
