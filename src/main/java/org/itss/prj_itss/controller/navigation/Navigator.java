package org.itss.prj_itss.controller.navigation;

public interface Navigator {
    void showView(String viewId);
    void showViewWithData(String viewId, Object data);
}
