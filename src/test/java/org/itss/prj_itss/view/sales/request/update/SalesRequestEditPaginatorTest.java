package org.itss.prj_itss.view.sales.request.update;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesRequestEditPaginatorTest {

    @Test
    void returnsFirstPageForEmptyList() {
        SalesRequestEditPaginator paginator = new SalesRequestEditPaginator();

        SalesRequestPaginationModel model = paginator.model(0);

        assertEquals(0, model.currentPage());
        assertEquals(1, model.totalPages());
        assertEquals(0, model.fromIndex());
        assertEquals(0, model.toIndex());
        assertFalse(model.hasPrevious());
        assertFalse(model.hasNext());
        assertEquals(List.of(1), model.pageNumbers());
    }

    @Test
    void returnsMiddlePageAfterNavigation() {
        SalesRequestEditPaginator paginator = new SalesRequestEditPaginator();
        paginator.goToPage(1);

        SalesRequestPaginationModel model = paginator.model(25);

        assertEquals(1, model.currentPage());
        assertEquals(3, model.totalPages());
        assertEquals(10, model.fromIndex());
        assertEquals(20, model.toIndex());
        assertTrue(model.hasPrevious());
        assertTrue(model.hasNext());
        assertEquals(List.of(1, 2, 3), model.pageNumbers());
    }

    @Test
    void clampsInvalidRequestedPageToLastAvailablePage() {
        SalesRequestEditPaginator paginator = new SalesRequestEditPaginator();
        paginator.goToPage(99);

        SalesRequestPaginationModel model = paginator.model(25);

        assertEquals(2, model.currentPage());
        assertEquals(20, model.fromIndex());
        assertEquals(25, model.toIndex());
        assertTrue(model.hasPrevious());
        assertFalse(model.hasNext());
    }

    @Test
    void returnsPageIndexForFocusedRow() {
        SalesRequestEditPaginator paginator = new SalesRequestEditPaginator();

        int pageIndex = paginator.focusPageForIndex(23);
        SalesRequestPaginationModel model = paginator.model(25);

        assertEquals(3, pageIndex);
        assertEquals(2, model.currentPage());
    }
}
