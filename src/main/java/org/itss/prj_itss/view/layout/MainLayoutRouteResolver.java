package org.itss.prj_itss.view.layout;

import org.itss.prj_itss.controller.navigation.Navigator;

public interface MainLayoutRouteResolver {
    boolean canResolve(String viewId);

    ResolvedLayoutView resolve(String viewId, Navigator navigator);

    void clearCache();
}
