package org.itss.prj_itss.controller.navigation;

public final class SimpleNavigator implements Navigator {
    private Navigator delegate;

    public void setDelegate(Navigator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void showView(String viewId) {
        if (delegate != null) {
            delegate.showView(viewId);
        }
    }

    @Override
    public void showViewWithData(String viewId, Object data) {
        if (delegate != null) {
            delegate.showViewWithData(viewId, data);
        }
    }
}
