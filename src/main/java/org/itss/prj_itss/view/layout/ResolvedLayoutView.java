package org.itss.prj_itss.view.layout;

import javafx.scene.Node;

public record ResolvedLayoutView(String viewId, String navTarget, Node node, Object viewInstance) {
}
