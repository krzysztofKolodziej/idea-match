package com.example.idea_match.shared.filter;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

public record PaginationRequest(Integer page, Integer size, String sort, String filter) {
    public PaginationRequest(Integer page, Integer size, String sort, String filter) {
        this.page = firstNonNull(page, 0);
        this.size = firstNonNull(size, 10);
        this.sort = firstNonNull(sort, "");
        this.filter = firstNonNull(filter, "");
    }

    public Pageable getPageable() {
        return PageRequest.of(page, size);
    }
}