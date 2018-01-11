import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { PosShingleEntry } from '../../../../services/api/generated/model/PosShingleEntry';
import { ArticleDTO } from '../../../../services/api/generated/model/ArticleDTO';


@Component({
    selector: 'app-pos-shingle-stats',
    templateUrl: './pos-shingle-stats.component.html',
    styleUrls: ['./pos-shingle-stats.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PosShingleStatsComponent implements OnInit {

    title: String = 'Part of Speech Shingle Stats';

    limit: number = null;

    pos: Array<string> = [];

    stats: Array<PosShingleEntry> = [];

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

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.stats = null;
        this.documentService.getPosShingleStats(this.pos, this.limit).subscribe(res => {
            this.stats = res.map(obj => {
                return {
                    key: Object.getOwnPropertyNames(obj)[0],
                    value: obj[Object.getOwnPropertyNames(obj)[0]]
                };
            });
        },
            err => {
                console.log('Error loading Part of Speech Stats!');
            });
    }
}
