package searchengine.dto.search;

import lombok.Data;


@Data
public class SearchResultResponse {
    private boolean result;
    private int count;
    private String error;
}
