package com.beginsecure.application_good.domain;

public class Pageable {
    private int pageSize; // elem de pe pagina
    private int pageNumber; // numarul paginii curente

    public Pageable(int pageSize, int pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public Pageable next() {
        return new Pageable(pageSize, pageNumber + 1);
    }

    public Pageable previous() {
        return new Pageable(pageSize, Math.max(0, pageNumber - 1));
    }
}

