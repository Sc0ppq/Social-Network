package com.beginsecure.application_good.domain;

import java.util.ArrayList;
import java.util.List;

public class Page<E> {
    private Iterable<E> elementsOnPage; // elem de pe pagina curenta
    private int totalNumberOfElements; // toate elementele

    public Page(Iterable<E> elementsOnPage, int totalNumberOfElements) {
        this.elementsOnPage = elementsOnPage;
        this.totalNumberOfElements = totalNumberOfElements;
    }

    public Iterable<E> getElementsOnPage() {
        return elementsOnPage;
    }

    public int getTotalNumberOfElements() {
        return totalNumberOfElements;
    }

}
