package searchengine.services.search;

import org.springframework.http.ResponseEntity;
import searchengine.dto.search.SearchResultResponse;

public interface SearchService {
    ResponseEntity<SearchResultResponse> search(String query, String site, int offset, int limit);
}
