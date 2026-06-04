package org.itss.prj_itss.controller.sales.request.create;

import java.util.List;

public interface ISalesRequestCreationActions {

    void submitRequested(List<SalesRequestCreationItemInput> items);
}
