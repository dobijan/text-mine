import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MaterialModule } from './material/material.module';
import { RouterModule, Routes } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent } from './app.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { DocumentsComponent } from './components/documents/documents.component';
import { ArticlesComponent } from './components/articles/articles.component';
import { NotesComponent } from './components/notes/notes.component';
import { AllNotesComponent } from './components/notes/subcomponents/all-notes/all-notes.component';
import { NotesByDocumentComponent } from './components/notes/subcomponents/notes-by-document/notes-by-document.component';
import { NotesByIdComponent } from './components/notes/subcomponents/notes-by-id/notes-by-id.component';
import { PersonsComponent } from './components/persons/persons.component';
import { LocationsComponent } from './components/locations/locations.component';
import { FlexLayoutModule } from '@angular/flex-layout';
import { NoteService } from './services/note-service/note.service';
import { NotesApi } from './services/api/generated/api/NotesApi';
import { DocumentsApi } from './services/api/generated/api/DocumentsApi';
import { ArticlesApi } from './services/api/generated/api/ArticlesApi';
import { CorporaApi } from './services/api/generated/api/CorporaApi';
import { PersonsApi } from './services/api/generated/api/PersonsApi';
import { LocationsApi } from './services/api/generated/api/LocationsApi';
import { HttpModule } from '@angular/http';
import { CorpusService } from './services/corpus-service/corpus.service';
import { DocumentService } from './services/document-service/document.service';
import { ArticleService } from './services/article-service/article.service';
import { FormsModule } from '@angular/forms';
import { ArticlesByIdComponent } from './components/articles/subcomponents/articles-by-id/articles-by-id.component';
import { ArticlesByDocumentComponent } from './components/articles/subcomponents/articles-by-document/articles-by-document.component';
import { FilterArticlesComponent } from './components/articles/subcomponents/filter-articles/filter-articles.component';
import { GroupArticlesByDocumentComponent } from './components/articles/subcomponents/group-articles-by-document/group-articles-by-document.component';
import { StatsComponent } from './components/stats/stats.component';
import { DocumentsByIdComponent } from './components/documents/subcomponents/documents-by-id/documents-by-id.component';
import { DocumentsByCorpusComponent } from './components/documents/subcomponents/documents-by-corpus/documents-by-corpus.component';
import { FilterDocumentsComponent } from './components/documents/subcomponents/filter-documents/filter-documents.component';
import { FilterSectionsComponent } from './components/documents/subcomponents/filter-sections/filter-sections.component';
import { FilterPagesComponent } from './components/documents/subcomponents/filter-pages/filter-pages.component';
import { FilterLinesComponent } from './components/documents/subcomponents/filter-lines/filter-lines.component';
import { PosStatsComponent } from './components/stats/subcomponents/pos-stats/pos-stats.component';
import { ShingleStatsComponent } from './components/stats/subcomponents/shingle-stats/shingle-stats.component';
import { PosShingleStatsComponent } from './components/stats/subcomponents/pos-shingle-stats/pos-shingle-stats.component';
import { SectionShingleStatsComponent } from './components/stats/subcomponents/section-shingle-stats/section-shingle-stats.component';
import { SectionPosShingleStatsComponent } from './components/stats/subcomponents/section-pos-shingle-stats/section-pos-shingle-stats.component';
import { PageShingleStatsComponent } from './components/stats/subcomponents/page-shingle-stats/page-shingle-stats.component';
import { PagePosShingleStatsComponent } from './components/stats/subcomponents/page-pos-shingle-stats/page-pos-shingle-stats.component';
import { LocationExistsComponent } from './components/locations/subcomponents/location-exists/location-exists.component';
import { LocationLineQueryComponent } from './components/locations/subcomponents/location-line-query/location-line-query.component';
import { LocationSectionQueryComponent } from './components/locations/subcomponents/location-section-query/location-section-query.component';
import { LocationPageQueryComponent } from './components/locations/subcomponents/location-page-query/location-page-query.component';
import { LocationSpatialQueryComponent } from './components/locations/subcomponents/location-spatial-query/location-spatial-query.component';
import { LocationSectionSpatialQueryComponent } from './components/locations/subcomponents/location-section-spatial-query/location-section-spatial-query.component';
import { LocationService } from './services/location-service/location.service';
import { PersonService } from './services/person-service/person.service';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { PersonExistsComponent } from './components/persons/subcomponents/person-exists/person-exists.component';
import { PersonSectionQueryComponent } from './components/persons/subcomponents/person-section-query/person-section-query.component';
import { PersonPageQueryComponent } from './components/persons/subcomponents/person-page-query/person-page-query.component';
import { PersonLineQueryComponent } from './components/persons/subcomponents/person-line-query/person-line-query.component';

const appRoutes: Routes = [
    {
        path: 'documents', component: DocumentsComponent, children: [
            { path: '', redirectTo: 'by-id', pathMatch: 'full' },
            { path: 'by-id', component: DocumentsByIdComponent },
            { path: 'by-corpus', component: DocumentsByCorpusComponent },
            { path: 'filter', component: FilterDocumentsComponent },
            { path: 'filter-sections', component: FilterSectionsComponent },
            { path: 'filter-pages', component: FilterPagesComponent },
            { path: 'filter-lines', component: FilterLinesComponent }
        ]
    },
    {
        path: 'articles', component: ArticlesComponent, children: [
            { path: '', redirectTo: 'by-id', pathMatch: 'full' },
            { path: 'by-id', component: ArticlesByIdComponent },
            { path: 'by-document', component: ArticlesByDocumentComponent },
            { path: 'filter', component: FilterArticlesComponent },
            { path: 'group-by-document', component: GroupArticlesByDocumentComponent }
        ]
    },
    {
        path: 'notes', component: NotesComponent, children: [
            { path: '', redirectTo: 'all', pathMatch: 'full' },
            { path: 'all', component: AllNotesComponent },
            { path: 'by-document', component: NotesByDocumentComponent },
            { path: 'by-id', component: NotesByIdComponent }
        ]
    },
    {
        path: 'locations', component: LocationsComponent, children: [
            { path: '', redirectTo: 'exists', pathMatch: 'full' },
            { path: 'exists', component: LocationExistsComponent },
            { path: 'section-query', component: LocationSectionQueryComponent },
            { path: 'page-query', component: LocationPageQueryComponent },
            { path: 'line-query', component: LocationLineQueryComponent },
            { path: 'spatial-query', component: LocationSpatialQueryComponent },
            { path: 'section-spatial-query', component: LocationSectionSpatialQueryComponent }
        ]
    },
    {
        path: 'persons', component: PersonsComponent, children: [
            { path: '', redirectTo: 'exists', pathMatch: 'full' },
            { path: 'exists', component: PersonExistsComponent },
            { path: 'section-query', component: PersonSectionQueryComponent },
            { path: 'page-query', component: PersonPageQueryComponent },
            { path: 'line-query', component: PersonLineQueryComponent }
        ]
    },
    {
        path: 'stats', component: StatsComponent, children: [
            { path: '', redirectTo: 'pos-stats', pathMatch: 'full' },
            { path: 'pos-stats', component: PosStatsComponent },
            { path: 'shingle-stats', component: ShingleStatsComponent },
            { path: 'pos-shingle-stats', component: PosShingleStatsComponent },
            { path: 'section-shingle-stats', component: SectionShingleStatsComponent },
            { path: 'section-pos-shingle-stats', component: SectionPosShingleStatsComponent },
            { path: 'page-shingle-stats', component: PageShingleStatsComponent },
            { path: 'page-pos-shingle-stats', component: PagePosShingleStatsComponent }
        ]
    },
    {
        path: '**',
        redirectTo: '/documents',
        pathMatch: 'full'
    }
];

@NgModule({
    declarations: [
        AppComponent,
        NavbarComponent,
        DocumentsComponent,
        ArticlesComponent,
        NotesComponent,
        AllNotesComponent,
        NotesByDocumentComponent,
        PersonsComponent,
        LocationsComponent,
        NotesByIdComponent,
        ArticlesByIdComponent,
        ArticlesByDocumentComponent,
        FilterArticlesComponent,
        GroupArticlesByDocumentComponent,
        StatsComponent,
        DocumentsByIdComponent,
        DocumentsByCorpusComponent,
        FilterDocumentsComponent,
        FilterSectionsComponent,
        FilterPagesComponent,
        FilterLinesComponent,
        PosStatsComponent,
        ShingleStatsComponent,
        PosShingleStatsComponent,
        SectionShingleStatsComponent,
        SectionPosShingleStatsComponent,
        PageShingleStatsComponent,
        PagePosShingleStatsComponent,
        LocationExistsComponent,
        LocationLineQueryComponent,
        LocationSectionQueryComponent,
        LocationPageQueryComponent,
        LocationSpatialQueryComponent,
        LocationSectionSpatialQueryComponent,
        PersonExistsComponent,
        PersonSectionQueryComponent,
        PersonPageQueryComponent,
        PersonLineQueryComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        MaterialModule,
        FlexLayoutModule,
        HttpModule,
        FormsModule,
        RouterModule.forRoot(
            appRoutes,
            { enableTracing: true }
        ),
        LeafletModule.forRoot()
    ],
    providers: [
        NoteService,
        DocumentService,
        ArticleService,
        CorpusService,
        LocationService,
        PersonService,
        NotesApi,
        DocumentsApi,
        ArticlesApi,
        LocationsApi,
        PersonsApi,
        CorporaApi
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
