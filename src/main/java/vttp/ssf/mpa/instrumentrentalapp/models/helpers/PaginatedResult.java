package vttp.ssf.mpa.instrumentrentalapp.models.helpers;

import java.util.List;

public class PaginatedResult<T> {
    private List<T> items;
    private int totalPages;

    public PaginatedResult(List<T> items, int totalPages) {
        this.items = items;
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalPages() {
        return totalPages;
    }
}

