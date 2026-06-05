package org.itss.prj_itss.view.sales.request.update;

import java.util.stream.IntStream;

final class SalesRequestEditPaginator {

    private static final int PAGE_SIZE = 10;

    private int currentPage;

    SalesRequestPaginationModel model(int totalItems) {
        int totalPages = pageCount(totalItems);
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, totalItems);
        return new SalesRequestPaginationModel(
            currentPage,
            totalPages,
            from,
            to,
            currentPage > 0,
            currentPage < totalPages - 1,
            IntStream.rangeClosed(1, totalPages).boxed().toList()
        );
    }

    void keepCurrentPageValid(int totalItems) {
        currentPage = Math.min(currentPage, Math.max(0, pageCount(totalItems) - 1));
    }

    int focusPageForIndex(int filteredIndex) {
        if (filteredIndex < 0) {
            return -1;
        }
        currentPage = filteredIndex / PAGE_SIZE;
        return filteredIndex % PAGE_SIZE;
    }

    void goToPage(int page) {
        currentPage = Math.max(0, page);
    }

    void previousPage() {
        currentPage = Math.max(0, currentPage - 1);
    }

    void nextPage() {
        currentPage++;
    }

    private int pageCount(int totalItems) {
        return Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
    }
}
