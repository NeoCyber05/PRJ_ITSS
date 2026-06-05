package org.itss.prj_itss.view.sales.request.update;

import java.util.List;

record SalesRequestPaginationModel(
        int currentPage,
        int totalPages,
        int fromIndex,
        int toIndex,
        boolean hasPrevious,
        boolean hasNext,
        List<Integer> pageNumbers
) {

    SalesRequestPaginationModel {
        pageNumbers = List.copyOf(pageNumbers);
    }
}
