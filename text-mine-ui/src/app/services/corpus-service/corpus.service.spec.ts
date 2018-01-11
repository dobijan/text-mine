import { TestBed, inject } from '@angular/core/testing';

import { CorpusService } from './corpus.service';

describe('CorpusService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CorpusService]
    });
  });

  it('should be created', inject([CorpusService], (service: CorpusService) => {
    expect(service).toBeTruthy();
  }));
});
