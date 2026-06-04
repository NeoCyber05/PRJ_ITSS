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

    // Open view packages to javafx.fxml for controller loading
    opens org.itss.prj_itss.view.layout to javafx.fxml;
    opens org.itss.prj_itss.view.auth to javafx.fxml;
    opens org.itss.prj_itss.view.home to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.site to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.order to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request.detail to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request.process.layout to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request.process.items to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request.process.suggest to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request.process.preview to javafx.fxml;
    opens org.itss.prj_itss.view.ordering.request.process.site to javafx.fxml;
    opens org.itss.prj_itss.view.sales.request.list to javafx.fxml;
    opens org.itss.prj_itss.view.sales.request.create to javafx.fxml;
    opens org.itss.prj_itss.view.sales.request.update to javafx.fxml;
    opens org.itss.prj_itss.view.sales.request.view to javafx.fxml;
    opens org.itss.prj_itss.view.sales.merchandise to javafx.fxml;
    opens org.itss.prj_itss.view.warehouse to javafx.fxml;
    opens org.itss.prj_itss.view.admin.account to javafx.fxml;
    opens org.itss.prj_itss.view.site.workspace to javafx.fxml;

    exports org.itss.prj_itss;
    exports org.itss.prj_itss.bootstrap;
    exports org.itss.prj_itss.controller.navigation;
}
