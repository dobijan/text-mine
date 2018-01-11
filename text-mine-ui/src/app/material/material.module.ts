import { NgModule } from '@angular/core';

import {
    MatMenuModule,
    MatToolbarModule,
    MatIconModule,
    MatCheckboxModule,
    MatInputModule,
    MatListModule,
    MatPaginatorModule,
    MatSelectModule,
    MatCardModule,
    MatTableModule,
    MatTabsModule,
    MatFormFieldModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatSidenavModule,
    MatProgressBarModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatSlideToggleModule
} from '@angular/material';

import { MatButtonModule } from '@angular/material/button';

@NgModule({
    imports: [
        MatButtonModule,
        MatMenuModule,
        MatToolbarModule,
        MatIconModule,
        MatCheckboxModule,
        MatInputModule,
        MatListModule,
        MatPaginatorModule,
        MatSelectModule,
        MatCardModule,
        MatTableModule,
        MatTabsModule,
        MatFormFieldModule,
        MatChipsModule,
        MatAutocompleteModule,
        MatSidenavModule,
        MatProgressBarModule,
        MatExpansionModule,
        MatSnackBarModule,
        MatSlideToggleModule
    ],
    exports: [
        MatButtonModule,
        MatMenuModule,
        MatToolbarModule,
        MatIconModule,
        MatCheckboxModule,
        MatInputModule,
        MatListModule,
        MatPaginatorModule,
        MatSelectModule,
        MatCardModule,
        MatTableModule,
        MatTabsModule,
        MatFormFieldModule,
        MatChipsModule,
        MatAutocompleteModule,
        MatSidenavModule,
        MatProgressBarModule,
        MatExpansionModule,
        MatSnackBarModule,
        MatSlideToggleModule
    ]
})
export class MaterialModule { }
