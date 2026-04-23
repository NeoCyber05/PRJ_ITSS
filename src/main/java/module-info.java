module org.itss.prj_itss {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires java.sql;

    opens org.itss.prj_itss.layout to javafx.fxml;
    opens org.itss.prj_itss.home to javafx.fxml;
    opens org.itss.prj_itss.site to javafx.fxml;
    opens org.itss.prj_itss.request to javafx.fxml;
    opens org.itss.prj_itss.order to javafx.fxml;
    exports org.itss.prj_itss;
    exports org.itss.prj_itss.entity;
    exports org.itss.prj_itss.db;
    exports org.itss.prj_itss.dao;
    exports org.itss.prj_itss.request;
    exports org.itss.prj_itss.order;
    exports org.itss.prj_itss.home;
    exports org.itss.prj_itss.site;
    exports org.itss.prj_itss.layout;
}
