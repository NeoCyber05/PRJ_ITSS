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
    requires java.desktop;

    opens org.itss.prj_itss.layout to javafx.fxml;
    opens org.itss.prj_itss.auth.presentation.login to javafx.fxml;
    opens org.itss.prj_itss.auth.presentation.workspace to javafx.fxml;
    opens org.itss.prj_itss.home to javafx.fxml;
    opens org.itss.prj_itss.site.presentation.ordering to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.received to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.detail to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.process to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.process.layout to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.process.items to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.process.suggest to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.process.preview to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.ordering.process.site to javafx.fxml;
    opens org.itss.prj_itss.order.presentation.ordering to javafx.fxml;
    opens org.itss.prj_itss.order.presentation.ordering.handle_cancellation to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.sales to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.sales.create to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.sales.update to javafx.fxml;
    opens org.itss.prj_itss.request.presentation.sales.view to javafx.fxml;
    opens org.itss.prj_itss.warehouse.presentation.confirm_arrival to javafx.fxml;
    exports org.itss.prj_itss;
    exports org.itss.prj_itss.db;
    exports org.itss.prj_itss.common.config;
    exports org.itss.prj_itss.auth.domain;
    exports org.itss.prj_itss.auth.application;
    exports org.itss.prj_itss.auth.application.port;
    exports org.itss.prj_itss.auth.infrastructure.persistence;
    exports org.itss.prj_itss.request.presentation.ordering.detail;
    exports org.itss.prj_itss.order.presentation.ordering;
    exports org.itss.prj_itss.home;
    exports org.itss.prj_itss.site.presentation.ordering;
    exports org.itss.prj_itss.layout;
}
