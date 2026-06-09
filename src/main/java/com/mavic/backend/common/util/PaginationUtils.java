package com.mavic.backend.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private PaginationUtils() {
    }

    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(page, Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
    }

    public static Pageable createPageable(int page, int size, String sortField) {
        return PageRequest.of(
                page,
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(sortField).ascending()
        );
    }
}
