package org.itss.prj_itss.controller.sales.request.create;

import org.itss.prj_itss.model.request.application.sales.shared.MerchandiseOption;

import java.util.List;

public interface ISalesRequestCreationViewPort {

    void bindEvents(ISalesRequestCreationActions actions);

    void render(SalesRequestCreationViewState viewModel);

    void showSuccess(String message);

    void showError(String message);

    void close();
}
