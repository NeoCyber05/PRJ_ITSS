package org.itss.prj_itss.bootstrap.navigation;

import org.itss.prj_itss.controller.navigation.Navigator;

public interface Route {
    boolean matches(String viewId);

    String canonicalViewId(String viewId);

    String navTarget(String viewId);

    boolean cacheable(String viewId);

    LoadedView load(String viewId, Navigator navigator) throws Exception;
}
