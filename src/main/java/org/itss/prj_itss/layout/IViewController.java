package org.itss.prj_itss.layout;

import org.itss.prj_itss.common.config.ApplicationContext;

public interface IViewController {
    void init(INavigator navigator, ApplicationContext context);

    default void onViewShown(String viewId) {
    }
}
