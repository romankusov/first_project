package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {
    IndexingResponse startIndexing() throws InterruptedException;
    IndexingResponse stopIndexing() throws InterruptedException;
}
