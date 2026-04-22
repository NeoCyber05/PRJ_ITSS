package org.itss.prj_itss.layout;

/**
 * Interface điều hướng — Views phụ thuộc interface này thay vì MainLayoutController.
 * Tuân thủ DIP: high-level modules (Views) không phụ thuộc low-level modules (Controller).
 */
public interface Navigator {
    void showView(String viewId);
}
