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
    opens org.itss.prj_itss.auth.login to javafx.fxml;
    opens org.itss.prj_itss.auth.workspace to javafx.fxml;
    opens org.itss.prj_itss.home to javafx.fxml;
    opens org.itss.prj_itss.ordering.site to javafx.fxml;
    opens org.itss.prj_itss.ordering.request.received to javafx.fxml;
    opens org.itss.prj_itss.ordering.request.detail to javafx.fxml;
    opens org.itss.prj_itss.ordering.request.process to javafx.fxml;
    opens org.itss.prj_itss.ordering.request.process.preview to javafx.fxml;
    opens org.itss.prj_itss.ordering.request.process.site to javafx.fxml;
    opens org.itss.prj_itss.ordering.order to javafx.fxml;
    opens org.itss.prj_itss.sales.request.create to javafx.fxml;
    opens org.itss.prj_itss.sales.request.update to javafx.fxml;
    opens org.itss.prj_itss.warehouse.order.confirm_arrival to javafx.fxml;
    exports org.itss.prj_itss;
    exports org.itss.prj_itss.entity;
    exports org.itss.prj_itss.db;
    exports org.itss.prj_itss.repository;
    exports org.itss.prj_itss.service;
    exports org.itss.prj_itss.dto;
    exports org.itss.prj_itss.model;
    exports org.itss.prj_itss.common.config;
    exports org.itss.prj_itss.ordering.request.detail;
    exports org.itss.prj_itss.ordering.order;
    exports org.itss.prj_itss.home;
    exports org.itss.prj_itss.ordering.site;
    exports org.itss.prj_itss.layout;
}
