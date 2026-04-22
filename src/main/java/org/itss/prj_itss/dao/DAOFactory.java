package org.itss.prj_itss.dao;


public class DAOFactory {

    private static DAOFactory instance;

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    public static void setInstance(DAOFactory custom) {
        instance = custom;
    }

    public IRequestDAO getRequestDAO() {
        return new RequestDAO();
    }

    public IOrderDAO getOrderDAO() {
        return new OrderDAO();
    }

    public ISiteDAO getSiteDAO() {
        return new SiteDAO();
    }

    public IInventoryDAO getInventoryDAO() {
        return new SiteDAO();
    }

    public IMerchandiseDAO getMerchandiseDAO() {
        return new MerchandiseDAO();
    }
}
