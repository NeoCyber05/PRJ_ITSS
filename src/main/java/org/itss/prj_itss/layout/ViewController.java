package org.itss.prj_itss.layout;

import org.itss.prj_itss.dao.DAOFactory;

public interface ViewController {
    void init(Navigator navigator, DAOFactory daoFactory);

    default void onViewShown(String viewId) {
    }
}
