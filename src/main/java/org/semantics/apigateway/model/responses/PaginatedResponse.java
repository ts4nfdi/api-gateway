package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class PaginatedResponse {
    public static int PAGE_SIZE = 50;

    @JsonIgnore
    private String base = "https://base4nfdi.org/ts4nfdi";

    @JsonProperty("totalItems")
    private final Long totalCount;

    private final Integer page;

    @JsonProperty("itemsPerPage")
    private final Integer pageSize = PAGE_SIZE;

    private final List<Map<String, Object>> member;

    @JsonProperty("@context")
    private final Map<String, Object> context = Map.of(
            "hydra", "http://www.w3.org/ns/hydra/core#",
            "Collection", "hydra:Collection",
            "collection", "hydra:member",
            "totalItems", "hydra:totalItems",
            "itemsPerPage", "hydra:itemsPerPage",
            "view", "hydra:view",
            "firstPage", "hydra:first",
            "lastPage", "hydra:last",
            "previousPage", "hydra:previous",
            "nextPage", "hydra:next"
    );

    @JsonProperty("@type")
    private final String type = "Collection";


    @JsonProperty("@id")
    public String id() {
        return String.format("%s/items?page=%s", this.base, this.page);
    }

    @JsonProperty("view")
    public Map<String, Object> view() {
        String base = this.base;
        return Map.of(
                "@id", this.id(),
                "@type", "hydra:PartialCollectionView",
                "firstPage", String.format("%s/items?page=%s", base, 1),
                "previousPage", String.format("%s/items?page=%s", base, this.page - 1),
                "nextPage", String.format("%s/items?page=%s", base, this.page + 1),
                "lastPage", String.format("%s/items?page=%s", base, this.getTotalPages())
        );
    }

    public PaginatedResponse(AggregatedApiResponse source) {
        this.totalCount = source.getTotalCount();
        this.page = source.getPage();
        this.member = source.getCollection();
    }

    @JsonProperty("totalPages")
    public Integer getTotalPages() {
        return (int) Math.ceil((double) this.totalCount / this.pageSize);
    }

    public PaginatedResponse() {
        this.totalCount = 0L;
        this.page = 1;
        this.member = Collections.emptyList();
    }

    public PaginatedResponse(List<Map<String, Object>> collection, long totalCount, int page) {
        this.totalCount = totalCount;
        this.page = page;
        this.member = collection;
    }
}
