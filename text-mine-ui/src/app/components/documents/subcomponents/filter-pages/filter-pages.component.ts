import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { ArticleDTO } from '../../../../services/api/generated/model/ArticleDTO';
import { Section } from '../../../../services/api/generated/model/Section';

@Component({
    selector: 'app-filter-pages',
    templateUrl: './filter-pages.component.html',
    styleUrls: ['./filter-pages.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class FilterPagesComponent implements OnInit {

    title: String = 'Filter Pages';

    selectedId: string = null;

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

    pages: Array<Section> = [];

    filters = {
        phrase: null,
        pos: null,
        slop: null,
        disjoint: false
    };

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.pages = null;
        this.documentService.getFilteredPages(this.selectedId, this.filters.phrase, this.filters.pos,
            this.filters.slop, this.filters.disjoint).subscribe(res => {
                this.pages = res.baseHits;
                this.progressMode = 'determinate';
                this.progressValue = 100;
            },
            err => {
                console.log('Error loading Sections!');
                this.progressMode = 'determinate';
                this.progressValue = 0;
            });
    }

}
