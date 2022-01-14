package io.github.niestrat99.advancedteleport.utilities;

import java.util.List;

public class PagedLists<T> {

    // Util by Thatsmusic99

    private final List<T> list;
    private final int pages;
    private final int contents;
    private int currentPage;
    private final int contentsPerPage;

    public PagedLists(List<T> list, int contentsPerPage) {
        if (contentsPerPage < 1) {
            throw new IllegalArgumentException("The provided int must be bigger than 0 for contents per page!");
        }
        this.list = list;
        int pages = 1;
        int bls = list.size();
        while (bls > contentsPerPage) {
            pages++;
            bls = bls - contentsPerPage;
        }
        this.pages = pages;
        this.contents = list.size();
        this.currentPage = 1;
        this.contentsPerPage = contentsPerPage;
    }

    public int getTotalPages() {
        return pages;
    }

    public int getTotalContents() {
        return contents;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private List<T> getList() {
        return list;
    }

    public List<T> getContentsInPage(int page) {
        if (page > getTotalPages()) {
            throw new IllegalArgumentException("The provided page is an int larger than the total number of pages!");
        }
        int sIndex = (page - 1) * getContentsPerPage();
        int eIndex = getContentsPerPage() + sIndex;
        if (eIndex > getList().size()) {
            eIndex = getList().size();
        }
        setPage(page);
        return getList().subList(sIndex, eIndex);
    }

    private void setPage(int page) {
        this.currentPage = page;
    }

    public int getContentsPerPage() {
        return contentsPerPage;
    }


}
