package org.itss.prj_itss.request.processing;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import org.itss.prj_itss.App;
import org.itss.prj_itss.common.config.ApplicationContext;
import org.itss.prj_itss.layout.Navigator;

import java.io.IOException;
import java.util.Objects;

public final class RequestProcessingView {

    private static final String VIEW_RESOURCE = "/org/itss/prj_itss/request/processing/request-processing-view.fxml";

    private final Node view;

    public RequestProcessingView(Navigator navigator, ApplicationContext context, int requestId) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(App.class.getResource(VIEW_RESOURCE)));
            this.view = loader.load();

            RequestProcessingController controller = loader.getController();
            controller.init(navigator, context);
            controller.setRequestId(requestId);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load request processing view", exception);
        }
    }

    public Node getView() {
        return view;
    }
}
